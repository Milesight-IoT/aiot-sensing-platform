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
package org.thingsboard.server.dao.timeseries;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.cache.ms.MsDeviceLastUpdateCache;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.EntityView;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.TelemetryConstants;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.EntityIdFactory;
import org.thingsboard.server.common.data.id.EntityViewId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.BaseDeleteTsKvQuery;
import org.thingsboard.server.common.data.kv.BaseReadTsKvQuery;
import org.thingsboard.server.common.data.kv.DeleteTsKvQuery;
import org.thingsboard.server.common.data.kv.LongDataEntry;
import org.thingsboard.server.common.data.kv.ReadTsKvQuery;
import org.thingsboard.server.common.data.kv.ReadTsKvQueryResult;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.kv.TsKvLatestRemovingResult;
import org.thingsboard.server.common.data.monitorDisk.GlobalSingletonParam;
import org.thingsboard.server.common.data.recognition.TelemetryExtData;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.common.data.recognition.TelemetryRecognitionThird;
import org.thingsboard.server.common.data.util.CollectionsUtil;
import org.thingsboard.server.common.enume.device.AbilityTypeEnum;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.dao.attributes.AttributesDao;
import org.thingsboard.server.dao.device.DeviceAbilityService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.entityview.EntityViewService;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.service.Validator;
import org.thingsboard.server.dao.sql.telemetry.JpaQueueTelemetryRecognitionDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.common.data.StringUtils.isBlank;

/**
 * @author Andrew Shvayka
 */
@SuppressWarnings("UnstableApiUsage")
@Service
@Slf4j
public class BaseTimeseriesService implements TimeseriesService {

    private static final int INSERTS_PER_ENTRY = 3;
    private static final int INSERTS_PER_ENTRY_WITHOUT_LATEST = 2;
    private static final int DELETES_PER_ENTRY = INSERTS_PER_ENTRY;
    public static final Function<List<Integer>, Integer> SUM_ALL_INTEGERS = new Function<>() {
        @Override
        public @Nullable Integer apply(@Nullable List<Integer> input) {
            int result = 0;
            if (input != null) {
                for (Integer tmp : input) {
                    if (tmp != null) {
                        result += tmp;
                    }
                }
            }
            return result;
        }
    };

    @Value("${database.ts_max_intervals}")
    private long maxTsIntervals;

    @Autowired
    private TimeseriesDao timeseriesDao;

    @Autowired
    private TimeseriesLatestDao timeseriesLatestDao;

    @Autowired
    private JpaQueueTelemetryRecognitionDao jpaQueueTelemetryRecognitionDao;

    @Autowired
    private AttributesDao attributesDao;

    @Autowired
    private DeviceAbilityService deviceAbilityService;
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private EntityViewService entityViewService;

    @Autowired
    private MsDeviceLastUpdateCache deviceLastUpdateCache;

    @Override
    public String findTelemetryImage(String deviceId, long ts, String ability, String entityType, TenantId tenantId) throws ExecutionException, InterruptedException {
        Aggregation agg = Aggregation.valueOf(Aggregation.NONE.name());
        List<ReadTsKvQuery> queries = toKeysList(ability).stream().map(key -> new BaseReadTsKvQuery(key, ts, ts + 1, 0L, 1, agg, "ASC"))
                .collect(Collectors.toList());
        EntityId entityId = EntityIdFactory.getByTypeAndId(entityType, deviceId);
        ListenableFuture<List<TsKvEntry>> list = findAll(tenantId, entityId, queries);
        List<TsKvEntry> tsKvEntries = list.get();
        if (tsKvEntries.size() == 0) {
            return "";
        }
        return tsKvEntries.get(0).getValueAsString();
    }

    private List<String> toKeysList(String keys) {
        List<String> keyList = null;
        if (!StringUtils.isEmpty(keys)) {
            keyList = Arrays.asList(keys.split(","));
        }
        return keyList;
    }

    @Override
    public ListenableFuture<List<ReadTsKvQueryResult>> findAllByQueries(TenantId tenantId, EntityId entityId, List<ReadTsKvQuery> queries) {
        validate(entityId);
        queries.forEach(this::validate);
        if (entityId.getEntityType().equals(EntityType.ENTITY_VIEW)) {
            EntityView entityView = entityViewService.findEntityViewById(tenantId, (EntityViewId) entityId);
            List<String> keys = entityView.getKeys() != null && entityView.getKeys().getTimeseries() != null
                    ? entityView.getKeys().getTimeseries() : Collections.emptyList();
            List<ReadTsKvQuery> filteredQueries =
                    queries.stream()
                            .filter(query -> keys.isEmpty() || keys.contains(query.getKey()))
                            .collect(Collectors.toList());
            return timeseriesDao.findAllAsync(tenantId, entityView.getEntityId(), updateQueriesForEntityView(entityView, filteredQueries));
        }
        return timeseriesDao.findAllAsync(tenantId, entityId, queries);
    }

    @Override
    public ListenableFuture<List<TsKvEntry>> findAll(TenantId tenantId, EntityId entityId, List<ReadTsKvQuery> queries) {
        return Futures.transform(findAllByQueries(tenantId, entityId, queries),
                result -> {
                    if (result != null && !result.isEmpty()) {
                        return result.stream().map(ReadTsKvQueryResult::getData).flatMap(Collection::stream).collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                }, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<Optional<TsKvEntry>> findLatest(TenantId tenantId, EntityId entityId, String key) {
        validate(entityId);
        return timeseriesLatestDao.findLatestOpt(tenantId, entityId, key);
    }

    @Override
    public ListenableFuture<List<TsKvEntry>> findLatest(TenantId tenantId, EntityId entityId, Collection<String> keys) {
        validate(entityId);
        List<ListenableFuture<TsKvEntry>> futures = Lists.newArrayListWithExpectedSize(keys.size());
        keys.forEach(key -> Validator.validateString(key, "Incorrect key " + key));
        keys.forEach(key -> futures.add(timeseriesLatestDao.findLatest(tenantId, entityId, key)));
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<TsKvEntry>> findAllLatest(TenantId tenantId, EntityId entityId) {
        validate(entityId);
        return timeseriesLatestDao.findAllLatest(tenantId, entityId);
    }

    @Override
    public List<String> findAllKeysByDeviceProfileId(TenantId tenantId, DeviceProfileId deviceProfileId) {
        return timeseriesLatestDao.findAllKeysByDeviceProfileId(tenantId, deviceProfileId);
    }

    @Override
    public List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds) {
        return timeseriesLatestDao.findAllKeysByEntityIds(tenantId, entityIds);
    }

    @Override
    public void cleanup(long systemTtl) {
        timeseriesDao.cleanup(systemTtl);
    }

    @Override
    public ListenableFuture<Integer> save(TenantId tenantId, EntityId entityId, TsKvEntry tsKvEntry) {
        validate(entityId);
        if (tsKvEntry == null) {
            throw new IncorrectParameterException("Key value entry can't be null");
        }
        ListenableFuture<Integer> futures = save(tenantId, entityId, List.of(tsKvEntry), 0L);
        return Futures.transform(Futures.allAsList(futures), SUM_ALL_INTEGERS, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<Integer> save(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntries, long ttl) {
        return doSave(tenantId, entityId, tsKvEntries, ttl, true);
    }


    @Override
    public ListenableFuture<Integer> saveWithoutLatest(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntries, long ttl) {
        return doSave(tenantId, entityId, tsKvEntries, ttl, false);
    }

    @Override
    public ListenableFuture<Integer> saveMs(TenantId tenantId, TbMsg msg, List<TsKvEntry> tsKvEntries, long ttl) {
        return doSaveMs(tenantId, msg, tsKvEntries, ttl, true);
    }

    @Override
    public ListenableFuture<Integer> saveWithoutLatestMs(TenantId tenantId, TbMsg msg, List<TsKvEntry> tsKvEntries, long ttl) {
        return doSaveMs(tenantId, msg, tsKvEntries, ttl, false);
    }

    private ListenableFuture<Integer> doSaveMs(TenantId tenantId, TbMsg msg, List<TsKvEntry> tsKvEntries, long ttl, boolean saveLatest) {
        EntityId entityId = msg.getOriginator();
        int inserts = saveLatest ? INSERTS_PER_ENTRY : INSERTS_PER_ENTRY_WITHOUT_LATEST;
        // 固定容量的List 节约内存的一种做法,
        List<ListenableFuture<Integer>> futures = Lists.newArrayListWithExpectedSize(tsKvEntries.size() * inserts);
        List<TelemetryRecognition> telemetryRecognitionList = saveData(tenantId, entityId, tsKvEntries, ttl, saveLatest, futures);
        msgDataAddInfo(msg, telemetryRecognitionList);
        return Futures.transform(Futures.allAsList(futures), SUM_ALL_INTEGERS, MoreExecutors.directExecutor());
    }

    private void msgDataAddInfo(TbMsg msg, List<TelemetryRecognition> telemetryRecognitionList) {
        EntityId originator = msg.getOriginator();
        if (!originator.getEntityType().equals(EntityType.DEVICE)) {
            log.debug("Not DEVICE type!ts = {}, entityId = {}", msg.getTs(), originator);
            return;
        }
        // 保存结果返回
        String data = msg.getData();
        ObjectNode objectNode = (ObjectNode) JacksonUtil.toJsonNode(data);
        boolean image = objectNode.has("image");
        if (!image) {
            return;
        }
        if (CollectionUtils.isEmpty(telemetryRecognitionList)) {
            log.debug("Telemetry awareness data cache not fetched ,ts = {}, entityId = {}", msg.getTs(), originator);
            return;
        }

        // 设备信息
        Map<String, Object> dataMap = new HashMap<>(msg.getMetaData().getData());
        dataMap.put("deviceId", originator.getId().toString());

        List<TelemetryRecognitionThird> thirds
                = telemetryRecognitionList.stream().map(TelemetryRecognitionThird::new).collect(Collectors.toList());

        TelemetryExtData telemetryExtData = TelemetryExtData.builder().deviceInfo(dataMap).telemetryRecognitionThirds(thirds).build();
        JsonNode jsonNode = JacksonUtil.valueToTree(telemetryExtData);
        objectNode.set("telemetryExtData", jsonNode);

        msg.setData(objectNode.toString());
    }

    private ListenableFuture<Integer> doSave(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntries, long ttl, boolean saveLatest) {
        int inserts = saveLatest ? INSERTS_PER_ENTRY : INSERTS_PER_ENTRY_WITHOUT_LATEST;
        // 固定容量的List 节约内存的一种做法,
        List<ListenableFuture<Integer>> futures = Lists.newArrayListWithExpectedSize(tsKvEntries.size() * inserts);
        saveData(tenantId, entityId, tsKvEntries, ttl, saveLatest, futures);
        return Futures.transform(Futures.allAsList(futures), SUM_ALL_INTEGERS, MoreExecutors.directExecutor());
    }

    private List<TelemetryRecognition> saveData(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntries, long ttl, boolean saveLatest, List<ListenableFuture<Integer>> futures) {
        List<TelemetryRecognition> imageTelemetryRecognitionList = new ArrayList<>();
        // 遥测数据表只存储 DEVICE 实体到Cassandra,感知数据到pgsql
        if (entityId.getEntityType().equals(EntityType.DEVICE)) {
            Device deviceById = deviceService.findDeviceById(tenantId, new DeviceId(entityId.getId()));
            log.info("Reception Msg EntityId: {} , DeviceName = {}", entityId.getId(), deviceById.getName());

            StopWatch stopWatch = new StopWatch();
            stopWatch.start("save device telemetryData all");

            List<String> imageAbilityList = new ArrayList<>();
            List<String> strAbilityList = new ArrayList<>();
            Map<String, TsKvEntry> strAbilityMap = new HashMap<>();
            long ts = 0;
            for (TsKvEntry tsKvEntry : tsKvEntries) {
                ts = tsKvEntry.getTs();
                if (TelemetryConstants.EXCLUDE_TELEMETRY_KEYS.contains(tsKvEntry.getKey())) {
                    continue;
                }
                if (entityId.getEntityType().equals(EntityType.ENTITY_VIEW)) {
                    throw new IncorrectParameterException("Telemetry data can't be stored for entity view. Read only");
                }
                if (saveLatest) {
                    // 保存最新遥测数据
                    futures.add(Futures.transform(
                            timeseriesLatestDao.saveLatest(tenantId, entityId, tsKvEntry),
                            v -> 0,
                            MoreExecutors.directExecutor()));
                }
                if (TelemetryConstants.IMAGE.equals(tsKvEntry.getKey())) {
                    // 图片数据保存到Cassandra
                    futures.add(timeseriesDao.savePartition(tenantId, entityId, tsKvEntry.getTs(), tsKvEntry.getKey()));
                    futures.add(timeseriesDao.save(tenantId, entityId, tsKvEntry, ttl));
                    GlobalSingletonParam.getInstance().addNum();
                    // 图片数据保存到postgresql
                    imageAbilityList.add(tsKvEntry.getKey());
                    // 通知更新图片了
                    deviceLastUpdateCache.put(entityId.getId().toString());
                } else {
                    strAbilityList.add(tsKvEntry.getKey());
                    strAbilityMap.put(tsKvEntry.getKey(), tsKvEntry);
                }
            }

            // 这个设备的所有能力
            List<DeviceAbility> deviceAbilityList = deviceAbilityService.findByDeviceId(entityId.getId());
            List<DeviceAbility> imageDeviceAbilityList = new ArrayList<>();
            List<DeviceAbility> strDeviceAbilityList = new ArrayList<>();
            deviceAbilityList.forEach(deviceAbility -> {
                short abilityType = deviceAbility.getAbilityType();
                if (AbilityTypeEnum.IMAGE.eq(abilityType)) {
                    imageDeviceAbilityList.add(deviceAbility);
                } else {
                    strDeviceAbilityList.add(deviceAbility);
                }
            });
            // 保存图片设备能力
            imageTelemetryRecognitionList = savePictureAbility(entityId, imageAbilityList, ts, imageDeviceAbilityList);
            // 保存字符串设备能力
            saveStrAbility(entityId, strAbilityList, strAbilityMap, ts, strDeviceAbilityList);
            // 保存设备最后活跃时间
            saveDeviceLastActivityTime(entityId, ts);
            stopWatch.stop();
            log.info("{} time consuming: {}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
        }
        return imageTelemetryRecognitionList;
    }

    private void saveStrAbility(EntityId entityId, List<String> strAbilityList, Map<String, TsKvEntry> strAbilityMap,
                                long ts, List<DeviceAbility> deviceAbilityList) {
        if (CollectionsUtil.isEmpty(strAbilityList)) {
            log.debug("String type telemetry capability does not exist,entityId = {} ,ts = {}", entityId, ts);
            return;
        }

        Map<String, DeviceAbility> deviceAbilityMap = deviceAbilityList.stream()
                .collect(Collectors.toMap(DeviceAbility::getAbility, value -> value, (v1, v2) -> v1));

        List<DeviceAbility> newDeviceAbilities = new ArrayList<>();
        strAbilityList.forEach(ability -> {
            DeviceAbility deviceAbility = deviceAbilityMap.get(ability);
            // 不存在就新增能力
            if (Objects.isNull(deviceAbility)) {
                DeviceAbility newDeviceAbility = DeviceAbility.builder()
                        .deviceId(entityId.getId())
                        .ability(ability)
                        .abilityType((short) 1)
                        .attributes(null)
                        .extraInfo(null)
                        .build();
                newDeviceAbilities.add(newDeviceAbility);
            }
            UUID uuid = Uuids.timeBased();
            TelemetryRecognition telemetryRecognition = TelemetryRecognition.builder()
                    .id(uuid)
                    .createdTime(Uuids.unixTimestamp(uuid))
                    .updatedTime(System.currentTimeMillis())
                    .deviceId(entityId.getId())
                    .ts(ts)
                    .ability(ability)
                    .abilityType((short) 1)
                    .value(strAbilityMap.get(ability).getValueAsString())
                    .extraInfo(null)
                    .build();
            jpaQueueTelemetryRecognitionDao.saveAddQueue(telemetryRecognition);
        });

        deviceAbilityService.saveAllSync(newDeviceAbilities);
    }

    private List<TelemetryRecognition> savePictureAbility(EntityId entityId, List<String> imageAbilityList, long ts,
                                                          List<DeviceAbility> imageDeviceAbilityList) {
        List<TelemetryRecognition> imageTelemetryRecognitionList = new ArrayList<>();

        imageAbilityList.forEach(imageKey -> imageDeviceAbilityList.forEach(deviceAbility -> {
            String attributes = deviceAbility.getAttributes();
            ObjectNode jsonNodes = JacksonUtil.newObjectNode();
            if (attributes != null) {
                for (String attribute : attributes.split(StringUtils.COMMA)) {
                    jsonNodes.put(attribute, "");
                }
            }

            UUID uuid = Uuids.timeBased();
            TelemetryRecognition telemetryRecognition = TelemetryRecognition.builder()
                    .id(uuid)
                    .createdTime(Uuids.unixTimestamp(uuid))
                    .updatedTime(System.currentTimeMillis())
                    .deviceId(entityId.getId())
                    .ts(ts)
                    .ability(deviceAbility.getAbility())
                    .abilityType(deviceAbility.getAbilityType())
                    .value(JacksonUtil.toString(jsonNodes))
                    .extraInfo(deviceAbility.getExtraInfo())
                    .build();
            imageTelemetryRecognitionList.add(telemetryRecognition);
            // 批量保存
            jpaQueueTelemetryRecognitionDao.saveAddQueue(telemetryRecognition);
        }));
        // 设置缓存
        imageTelemetryRecognitionList.removeIf(telemetryRecognition -> "image".equals(telemetryRecognition.getAbility()));
        return imageTelemetryRecognitionList;
    }

    private void saveDeviceLastActivityTime(EntityId entityId, long ts) {
        LongDataEntry longDataEntry = new LongDataEntry("lastActivityTime", ts);
        BaseAttributeKvEntry lastActivityTime = new BaseAttributeKvEntry(longDataEntry, System.currentTimeMillis());
        attributesDao.save(null, entityId, DataConstants.SERVER_SCOPE, lastActivityTime);
    }

    @Override
    public ListenableFuture<List<Void>> saveLatest(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntries) {
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(tsKvEntries.size());
        for (TsKvEntry tsKvEntry : tsKvEntries) {
            if (tsKvEntry == null) {
                throw new IncorrectParameterException("Key value entry can't be null");
            }
            futures.add(timeseriesLatestDao.saveLatest(tenantId, entityId, tsKvEntry));
        }
        return Futures.allAsList(futures);
    }


    private List<ReadTsKvQuery> updateQueriesForEntityView(EntityView entityView, List<ReadTsKvQuery> queries) {
        return queries.stream().map(query -> {
            long startTs;
            if (entityView.getStartTimeMs() != 0 && entityView.getStartTimeMs() > query.getStartTs()) {
                startTs = entityView.getStartTimeMs();
            } else {
                startTs = query.getStartTs();
            }

            long endTs;
            if (entityView.getEndTimeMs() != 0 && entityView.getEndTimeMs() < query.getEndTs()) {
                endTs = entityView.getEndTimeMs();
            } else {
                endTs = query.getEndTs();
            }
            return new BaseReadTsKvQuery(query, startTs, endTs);
        }).collect(Collectors.toList());
    }

    @Override
    public ListenableFuture<List<TsKvLatestRemovingResult>> remove(TenantId tenantId, EntityId entityId, List<DeleteTsKvQuery> deleteTsKvQueries) {
        validate(entityId);
        deleteTsKvQueries.forEach(BaseTimeseriesService::validate);
        List<ListenableFuture<TsKvLatestRemovingResult>> futures = Lists.newArrayListWithExpectedSize(deleteTsKvQueries.size() * DELETES_PER_ENTRY);
        for (DeleteTsKvQuery tsKvQuery : deleteTsKvQueries) {
            deleteAndRegisterFutures(tenantId, futures, entityId, tsKvQuery);
        }
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<TsKvLatestRemovingResult>> removeLatest(TenantId tenantId, EntityId entityId, Collection<String> keys) {
        validate(entityId);
        List<ListenableFuture<TsKvLatestRemovingResult>> futures = Lists.newArrayListWithExpectedSize(keys.size());
        for (String key : keys) {
            DeleteTsKvQuery query = new BaseDeleteTsKvQuery(key, 0, System.currentTimeMillis(), false);
            futures.add(timeseriesLatestDao.removeLatest(tenantId, entityId, query));
        }
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<Collection<String>> removeAllLatest(TenantId tenantId, EntityId entityId) {
        validate(entityId);
        return Futures.transformAsync(this.findAllLatest(tenantId, entityId), latest -> {
            if (latest != null && !latest.isEmpty()) {
                Collection<String> keys = latest.stream().map(TsKvEntry::getKey).collect(Collectors.toList());
                return Futures.transform(this.removeLatest(tenantId, entityId, keys), res -> keys, MoreExecutors.directExecutor());
            } else {
                return Futures.immediateFuture(Collections.emptyList());
            }
        }, MoreExecutors.directExecutor());
    }

    private void deleteAndRegisterFutures(TenantId tenantId, List<ListenableFuture<TsKvLatestRemovingResult>> futures, EntityId entityId, DeleteTsKvQuery query) {
        futures.add(Futures.transform(timeseriesDao.remove(tenantId, entityId, query), v -> null, MoreExecutors.directExecutor()));
        futures.add(timeseriesLatestDao.removeLatest(tenantId, entityId, query));
    }

    private static void validate(EntityId entityId) {
        Validator.validateEntityId(entityId, "Incorrect entityId " + entityId);
    }

    private void validate(ReadTsKvQuery query) {
        if (query == null) {
            throw new IncorrectParameterException("ReadTsKvQuery can't be null");
        } else if (isBlank(query.getKey())) {
            throw new IncorrectParameterException("Incorrect ReadTsKvQuery. Key can't be empty");
        } else if (query.getAggregation() == null) {
            throw new IncorrectParameterException("Incorrect ReadTsKvQuery. Aggregation can't be empty");
        }
        if (!Aggregation.NONE.equals(query.getAggregation())) {
            long step = Math.max(query.getInterval(), 1000);
            long intervalCounts = (query.getEndTs() - query.getStartTs()) / step;
            if (intervalCounts > maxTsIntervals || intervalCounts < 0) {
                throw new IncorrectParameterException("Incorrect TsKvQuery. Number of intervals is to high - " + intervalCounts + ". " +
                        "Please increase 'interval' parameter for your query or reduce the time range of the query.");
            }
        }
    }


    private static void validate(DeleteTsKvQuery query) {
        if (query == null) {
            throw new IncorrectParameterException("DeleteTsKvQuery can't be null");
        } else if (isBlank(query.getKey())) {
            throw new IncorrectParameterException("Incorrect DeleteTsKvQuery. Key can't be empty");
        }
    }
}
