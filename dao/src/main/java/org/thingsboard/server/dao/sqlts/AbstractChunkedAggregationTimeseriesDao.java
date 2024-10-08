/*
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.thingsboard.server.dao.sqlts;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.BatchDeleteTsKv;
import org.thingsboard.server.common.data.kv.DeleteTsKvQuery;
import org.thingsboard.server.common.data.kv.ReadTsKvQuery;
import org.thingsboard.server.common.data.kv.ReadTsKvQueryResult;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.stats.StatsFactory;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.AbstractTsKvEntity;
import org.thingsboard.server.dao.model.sqlts.ts.TsKvEntity;
import org.thingsboard.server.dao.sql.TbSqlBlockingQueueParams;
import org.thingsboard.server.dao.sql.TbSqlBlockingQueueWrapper;
import org.thingsboard.server.dao.sqlts.insert.InsertTsRepository;
import org.thingsboard.server.dao.sqlts.ts.TsKvRepository;
import org.thingsboard.server.dao.timeseries.QueryCursor;
import org.thingsboard.server.dao.timeseries.SimpleListenableFuture;
import org.thingsboard.server.dao.timeseries.TimeseriesDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
public abstract class AbstractChunkedAggregationTimeseriesDao extends AbstractSqlTimeseriesDao implements TimeseriesDao {

    @Autowired
    protected TsKvRepository tsKvRepository;

    @Autowired
    protected InsertTsRepository<TsKvEntity> insertRepository;

    protected TbSqlBlockingQueueWrapper<TsKvEntity> tsQueue;
    @Autowired
    private StatsFactory statsFactory;


    @Override
    public long toPartitionTs(long ts) {
        return ts;
    }

    @Override
    public ListenableFuture<Void> deleteBatchAsync(TenantId tenantId, BatchDeleteTsKv query, SimpleListenableFuture<Void> resultFuture) {
        List<UUID> devices = query.getEntityIds();
        List<TsKvEntity> tsKvEntities = new ArrayList<>();
        devices.stream().forEach(obj -> {
            TsKvEntity tsKvEntity = new TsKvEntity();
            tsKvEntity.setEntityId(obj);
            String strKey = query.getKey();
            Integer keyId = getOrSaveKeyId(strKey);
            tsKvEntity.setKey(keyId);
            tsKvEntities.add(tsKvEntity);
        });
        Iterable<TsKvEntity> iterable = tsKvEntities;
        return service.submit(() -> {
            tsKvRepository.deleteAllInBatch(iterable);
            return null;
        });
    }
    @Override
    public ListenableFuture<Void> deleteByAsync(TenantId tenantId, QueryCursor query, SimpleListenableFuture<Void> resultFuture){
        String strKey = query.getKey();
        Integer keyId = getOrSaveKeyId(strKey);
        return service.submit(() -> {
            tsKvRepository.delete(
                    query.getEntityId(),
                    keyId,
                    query.getStartTs(),
                    query.getEndTs());
            return null;
        });
    }

    @PostConstruct
    protected void init() {
        TbSqlBlockingQueueParams tsParams = TbSqlBlockingQueueParams.builder()
                .logName("TS")
                .batchSize(tsBatchSize)
                .maxDelay(tsMaxDelay)
                .statsPrintIntervalMs(tsStatsPrintIntervalMs)
                .statsNamePrefix("ts")
                .batchSortEnabled(batchSortEnabled)
                .build();

        Function<TsKvEntity, Integer> hashcodeFunction = entity -> entity.getEntityId().hashCode();
        tsQueue = new TbSqlBlockingQueueWrapper<>(tsParams, hashcodeFunction, tsBatchThreads, statsFactory);
        tsQueue.init(logExecutor, v -> insertRepository.saveOrUpdate(v),
                Comparator.comparing((Function<TsKvEntity, UUID>) AbstractTsKvEntity::getEntityId)
                        .thenComparing(AbstractTsKvEntity::getKey)
                        .thenComparing(AbstractTsKvEntity::getTs)
        );
    }

    @PreDestroy
    protected void destroy() {
        if (tsQueue != null) {
            tsQueue.destroy();
        }
    }

    @Override
    public ListenableFuture<Void> remove(TenantId tenantId, EntityId entityId, DeleteTsKvQuery query) {
        return service.submit(() -> {
            tsKvRepository.delete(
                    entityId.getId(),
                    getOrSaveKeyId(query.getKey()),
                    query.getStartTs(),
                    query.getEndTs());
            return null;
        });
    }

    @Override
    public ListenableFuture<Integer> savePartition(TenantId tenantId, EntityId entityId, long tsKvEntryTs, String key) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<List<ReadTsKvQueryResult>> findAllAsync(TenantId tenantId, EntityId entityId, List<ReadTsKvQuery> queries) {
        return processFindAllAsync(tenantId, entityId, queries);
    }

    @Override
    public ListenableFuture<ReadTsKvQueryResult> findAllAsync(TenantId tenantId, EntityId entityId, ReadTsKvQuery query) {
        if (query.getAggregation() == Aggregation.NONE) {
            return Futures.immediateFuture(findAllAsyncWithLimit(entityId, query));
        } else {
            List<ListenableFuture<Optional<TsKvEntity>>> futures = new ArrayList<>();
            long startPeriod = query.getStartTs();
            long endPeriod = Math.max(query.getStartTs() + 1, query.getEndTs());
            long step = query.getInterval();
            while (startPeriod < endPeriod) {
                long startTs = startPeriod;
                long endTs = Math.min(startPeriod + step, endPeriod);
                long ts = startTs + (endTs - startTs) / 2;
                ListenableFuture<Optional<TsKvEntity>> aggregateTsKvEntry = findAndAggregateAsync(entityId, query.getKey(), startTs, endTs, ts, query.getAggregation());
                futures.add(aggregateTsKvEntry);
                startPeriod = endTs;
            }
            return getReadTsKvQueryResultFuture(query, Futures.allAsList(futures));
        }
    }

    private ReadTsKvQueryResult findAllAsyncWithLimit(EntityId entityId, ReadTsKvQuery query) {
        Integer keyId = getOrSaveKeyId(query.getKey());
        List<TsKvEntity> tsKvEntities = tsKvRepository.findAllWithLimit(
                entityId.getId(),
                keyId,
                query.getStartTs(),
                query.getEndTs(),
                PageRequest.ofSize(query.getLimit()).withSort(Direction.fromString(query.getOrder()), "ts"));
        tsKvEntities.forEach(tsKvEntity -> tsKvEntity.setStrKey(query.getKey()));
        List<TsKvEntry> tsKvEntries = DaoUtil.convertDataList(tsKvEntities);
        long lastTs = tsKvEntries.stream().map(TsKvEntry::getTs).max(Long::compare).orElse(query.getStartTs());
        return new ReadTsKvQueryResult(query.getId(), tsKvEntries, lastTs);
    }

    ListenableFuture<Optional<TsKvEntity>> findAndAggregateAsync(EntityId entityId, String key, long startTs, long endTs, long ts, Aggregation aggregation) {
        return service.submit(() -> {
            TsKvEntity entity = switchAggregation(entityId, key, startTs, endTs, aggregation);
            if (entity != null && entity.isNotEmpty()) {
                entity.setEntityId(entityId.getId());
                entity.setStrKey(key);
                entity.setTs(ts);
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }
        });
    }

    protected TsKvEntity switchAggregation(EntityId entityId, String key, long startTs, long endTs, Aggregation aggregation) {
        var keyId = getOrSaveKeyId(key);
        switch (aggregation) {
            case AVG:
                return tsKvRepository.findAvg(entityId.getId(), keyId, startTs, endTs);
            case MAX:
                var max = tsKvRepository.findNumericMax(entityId.getId(), keyId, startTs, endTs);
                if (max.isNotEmpty()) {
                    return max;
                } else {
                    return tsKvRepository.findStringMax(entityId.getId(), keyId, startTs, endTs);
                }
            case MIN:
                var min = tsKvRepository.findNumericMin(entityId.getId(), keyId, startTs, endTs);
                if (min.isNotEmpty()) {
                    return min;
                } else {
                    return tsKvRepository.findStringMin(entityId.getId(), keyId, startTs, endTs);
                }
            case SUM:
                return tsKvRepository.findSum(entityId.getId(), keyId, startTs, endTs);
            case COUNT:
                return tsKvRepository.findCount(entityId.getId(), keyId, startTs, endTs);
            default:
                throw new IllegalArgumentException("Not supported aggregation type: " + aggregation);
        }
    }
}
