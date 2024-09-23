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

package org.thingsboard.server.service.system;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.TelemetryConstants;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.BaseDeleteTsKvQuery;
import org.thingsboard.server.common.data.kv.DeleteTsKvQuery;
import org.thingsboard.server.common.data.monitorDisk.GlobalSingletonParam;
import org.thingsboard.server.common.data.monitorDisk.GroupEntity;
import org.thingsboard.server.common.enume.MsAppContent;
import org.thingsboard.server.dao.model.sql.TelemetryRecognitionEntity;
import org.thingsboard.server.dao.timeseries.QueryCursor;
import org.thingsboard.server.dao.timeseries.SimpleListenableFuture;
import org.thingsboard.server.dao.timeseries.TelemetryRecognitionDao;
import org.thingsboard.server.dao.timeseries.TimeseriesDao;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.utils.ExecuteCommandUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.thingsboard.common.util.SystemUtil.getCassandraSpaceAvailable;
import static org.thingsboard.common.util.SystemUtil.getCassandraSpaceTotal;

/**
 * @author zhangzy
 */
@TbCoreComponent
@Service
@RequiredArgsConstructor
@Slf4j
public class DiskRecycleService {

    @Value("${cassandra.compressed_directory}")
    private String compressedDirectory;
    @Value("${cassandra.delete_sleep_time}")
    private int sleepTime;
    @Value("${cassandra.disk_alarm_space}")
    private long diskAlarmSpace;
    @Value("${cassandra.ms_min_free_space_in_mb}")
    private long msMinFreeSpaceInMb;
    /**
     * delete_pattern配置，表示删除模式，ROUND：循环删除，PERCENTAGE：百分比
     */
    @Value("${cassandra.delete_pattern}")
    private String deletePattern;
    /**
     * PERCENTAGE：百分比 默认总量的10%
     */
    @Value("${cassandra.delete_percentage}")
    private int deletePercentage;
    @Value("${cassandra.strategy_round}")
    private int strategyRound;
    @Value("${cassandra.limit_num}")
    private int limitNum;
    /**
     * 删除次数默认20次
     */
    @Value("${cassandra.delete_number_threshold}")
    private int deleteNumberThreshold;
    @Autowired
    private TelemetryRecognitionDao telemetryRecognitionDao;
    @Autowired
    private TimeseriesDao timeseriesDao;

    /**
     * 是否正在进行
     */
    private volatile boolean isRunning;

    public void checkCassandraDisk() {
        GlobalSingletonParam param = calculateDiskSpace();
        if (param == null) {
            return;
        }
        long cassandraSpaceUsage = param.getCassandraSpaceUsage();
        long cassandraSpaceTotal = param.getCassandraSpaceTotal();
        // yml配置告警空间, 百分比转换
        long alarmSpace = cassandraSpaceTotal * diskAlarmSpace / 100;

        if (cassandraSpaceUsage < alarmSpace) {
            if (isRunning) {
                return;
            }
            if (param.getNumberOfTimesNeedsToBeDeleted() <= 0) {
                // 计算这个轮次需要删除多少数据
                requiresTheRemovalOfTurns(param);
            }
            int addCountNum = param.getAddCountNum().get();
            long lastRemainDisk = param.getLastRemainDisk().get();
            // 增加数量大于要删除数量
            boolean numDelete = limitNum * strategyRound < addCountNum;

            if (0 == lastRemainDisk || cassandraSpaceUsage < lastRemainDisk || numDelete) {
                log.warn("cassandra cassandraSpaceUsage = {}GB ,cassandraSpaceTotal = {}GB, lastRemainDisk={}GB, alarmSpace = {}GB , diskAlarmSpace={}%",
                        cassandraSpaceUsage, cassandraSpaceTotal, lastRemainDisk, alarmSpace, diskAlarmSpace);
                log.warn("addCountNum = {} , maximumNumberOfDeletions = {}", addCountNum, limitNum * strategyRound);
                // 初始化
                GlobalSingletonParam.getInstance().deleteAddNum(cassandraSpaceUsage);
                // 删除执行最后一次已使用磁盘大小
                param.getLastRemainDisk().set(cassandraSpaceUsage);
                // 重置删除计数
                param.getDeleteNum().set(0);
                // 删除
                cleanCassandraSpace();
            }
        } else {
            // 重置
            GlobalSingletonParam.getInstance().reset();
        }
    }

    private void requiresTheRemovalOfTurns(GlobalSingletonParam param) {
        if (MsAppContent.ROUND.equals(deletePattern)) {
            param.setNumberOfTimesNeedsToBeDeleted(deleteNumberThreshold);
        } else {
            int imageTotal = telemetryRecognitionDao.countImageTotal();
            if (imageTotal <= 0) {
                param.setNumberOfTimesNeedsToBeDeleted(0);
            } else {
                int needDeleteNum = imageTotal * deletePercentage / 100;
                int numberOfTimesNeedsToBeDeleted = needDeleteNum / (limitNum * strategyRound);
                param.setNumberOfTimesNeedsToBeDeleted(numberOfTimesNeedsToBeDeleted);
            }
        }
        int o1 = param.getNumberOfTimesNeedsToBeDeleted() * limitNum * strategyRound;
        log.info("Delete pattern is : {} ,the {} times needs to be removed ,the total number of projected deletions = {}strip",
                deletePattern, param.getNumberOfTimesNeedsToBeDeleted(), o1);
    }

    private GlobalSingletonParam calculateDiskSpace() {
        Optional<Long> spaceAvailable = getCassandraSpaceAvailable(compressedDirectory);
        Optional<Long> spaceTotal = getCassandraSpaceTotal(compressedDirectory);
        // 磁盘剩余空间 GB, 值不能过小,压缩过程中,磁盘占用量是增加的,压缩完成后才释放
        if (spaceAvailable.isEmpty() || spaceTotal.isEmpty()) {
            return null;
        }
        // 磁盘可用空间 M
        GlobalSingletonParam param = GlobalSingletonParam.getInstance();
        long cassandraSpaceUsageM = spaceAvailable.get() / (1024 * 1024);
        if (cassandraSpaceUsageM <= msMinFreeSpaceInMb) {
            log.warn("Cassandra space usage is too small, cannot save data! please deal with it in time ! cassandraSpaceUsageM = {}M", cassandraSpaceUsageM);
            param.setCanSaveData(false);
        } else {
            param.setCanSaveData(true);
        }

        // 磁盘可用空间 G
        param.setCassandraSpaceUsage(spaceAvailable.get() / (1024 * 1024 * 1024));
        // 磁盘总空间
        long cassandraSpaceTotal = spaceTotal.get() / (1024 * 1024 * 1024);
        param.setCassandraSpaceTotal(cassandraSpaceTotal);
        return param;
    }

    private void sleep(int time) {
        // 休眠时间为0不等待
        if (sleepTime == 0) {
            return;
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 执行释放磁盘空间操作
     */
    public void cleanCassandraSpace() {
        try {
            isRunning = true;
            // 删除Cassandra数据库ts_kv和 postgresql的telemetry_recognition表数据
            deleteHistoryData();
            // 执行删除N次后执行 nodetool garbagecollect msaiotsensingplatform ts_kv_cf这个表 回收墓碑
            nodetoolGarbagecollect();
        } catch (Exception e) {
            log.error("cleanCassandraSpace error ", e);
        } finally {
            isRunning = false;
        }
    }

    private void nodetoolGarbagecollect() {
        // 计数+1
        GlobalSingletonParam.getInstance().getNumberOfDeletionExecutions().addAndGet(1);

        GlobalSingletonParam instance = GlobalSingletonParam.getInstance();
        int numberOfDeletionExecutions = instance.getNumberOfDeletionExecutions().get();
        int numberOfTimesNeedsToBeDeleted = instance.getNumberOfTimesNeedsToBeDeleted();

        boolean deletionIsSufficient = numberOfTimesNeedsToBeDeleted == 0 || numberOfDeletionExecutions % numberOfTimesNeedsToBeDeleted == 0;
        if (numberOfDeletionExecutions >= numberOfTimesNeedsToBeDeleted && deletionIsSufficient) {
            ExecuteCommandUtil.nodetool("nodetool garbagecollect msaiotsensingplatform ts_kv_cf");
            GlobalSingletonParam param = calculateDiskSpace();
            if (param != null) {
                long cassandraSpaceUsage = param.getCassandraSpaceUsage();
                long cassandraSpaceTotal = param.getCassandraSpaceTotal();
                long alarmSpace = cassandraSpaceTotal * diskAlarmSpace / 100;
                long lastRemainDisk = param.getLastRemainDisk().get();

                log.warn("After nodetool garbagecollect , Cassandra cassandraSpaceUsage = {}GB ,cassandraSpaceTotal = {}GB, lastRemainDisk={}GB, alarmSpace = {}GB , diskAlarmSpace={}%",
                        cassandraSpaceUsage, cassandraSpaceTotal, lastRemainDisk, alarmSpace, diskAlarmSpace);
            }
        }
    }

    public void deleteHistoryData() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("cleanCassandraSpace");
        final SimpleListenableFuture<Void> resultFuture = new SimpleListenableFuture<>();
        // 最小ts
        Long oldestTs = telemetryRecognitionDao.findMinTs();
        if (oldestTs == null) {
            log.warn("there is no data, skipping!");
            return;
        }
        boolean nextGroup = true;
        GroupEntity groupEntity = new GroupEntity(oldestTs, new AtomicInteger(), TenantId.SYS_TENANT_ID);
        while (nextGroup) {
            GroupEntity resultEntity = removeData(resultFuture, groupEntity);
            if (resultEntity != null) {
                nextGroup = resultEntity.getNextGroup();
                groupEntity = resultEntity;
            }
        }

        int deleteCount = groupEntity.getAtomicInteger().get();
        GlobalSingletonParam.getInstance().getDeleteNum().addAndGet(deleteCount);
        stopWatch.stop();
        log.warn("Cassandra delete {} time ,data number: {} , use time {} seconds"
                , GlobalSingletonParam.getInstance().getNumberOfDeletionExecutions().get(), deleteCount, stopWatch.getTotalTimeSeconds());
    }

    public GroupEntity removeData(SimpleListenableFuture<Void> resultFuture, GroupEntity groupEntity) {
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(limitNum);
        long oldestTs = groupEntity.getMinTs();
        // 计算条数
        AtomicInteger atomicInteger = groupEntity.getAtomicInteger();
        atomicInteger.addAndGet(1);
        TenantId tenantId = groupEntity.getTenantId();
        // 查找要删除数据
        List<TelemetryRecognitionEntity> result = telemetryRecognitionDao.findOldTelemetryRecognition(oldestTs, limitNum);
        // 多删除一轮
        boolean nextGroup = strategyRound != atomicInteger.get() && result.size() >= limitNum;
        int resultSize = result.size();

        long lastTs = result.get(resultSize - 1).getTs();
        // 删除数据
        circularDbDelete(result, groupEntity.getTenantId(), futures, resultFuture);
        if (!nextGroup) {
            atomicInteger.set((atomicInteger.get() - 1) * limitNum + resultSize);
        }
        // 本组最大ts作为下一组最小ts
        oldestTs = lastTs;
        return new GroupEntity(oldestTs, lastTs, nextGroup, atomicInteger, tenantId);
    }

    /**
     * 循环删除
     *
     * @param result       遥测数据实体
     * @param tenantId     租户ID
     * @param futures      异步
     * @param resultFuture 当前队列
     */
    private void circularDbDelete(List<TelemetryRecognitionEntity> result, TenantId tenantId, List<ListenableFuture<Void>> futures, SimpleListenableFuture<Void> resultFuture) {
        for (TelemetryRecognitionEntity entity : result) {
            long ts = entity.getTs();
            DeleteTsKvQuery query = new BaseDeleteTsKvQuery(TelemetryConstants.IMAGE, ts, (ts + 1L), false);
            long partition = timeseriesDao.toPartitionTs(ts);
            QueryCursor cursor = new QueryCursor(EntityType.DEVICE.toString(), entity.getDeviceId(), query, List.of(partition));
            // 停顿一会，减轻Cassandra压力
            sleep(sleepTime);
            futures.add(timeseriesDao.deleteByAsync(tenantId, cursor, resultFuture));
            // 删除telemetryRecognition感知数据
            telemetryRecognitionDao.deleteByTsAndDeviceId(ts, entity.getDeviceId());
        }
    }

}
