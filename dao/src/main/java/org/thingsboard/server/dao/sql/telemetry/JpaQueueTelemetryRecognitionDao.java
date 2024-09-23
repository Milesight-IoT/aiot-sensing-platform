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

package org.thingsboard.server.dao.sql.telemetry;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.common.stats.StatsFactory;
import org.thingsboard.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import org.thingsboard.server.dao.sql.ScheduledLogExecutorComponent;
import org.thingsboard.server.dao.sql.TbSqlBlockingQueueParams;
import org.thingsboard.server.dao.sql.TbSqlBlockingQueueWrapper;
import org.thingsboard.server.dao.util.SqlDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 感知数据存储(队列)
 *
 * @author Luohh
 */
@Slf4j
@Component
@SqlDao
public class JpaQueueTelemetryRecognitionDao extends JpaAbstractDaoListeningExecutorService {

    @Autowired
    private JpaTelemetryRecognitionDao jpaTelemetryRecognitionDao;
    @Autowired
    ScheduledLogExecutorComponent logExecutor;
    @Autowired
    private StatsFactory statsFactory;

    @Value("${sql.tr.batch_size:1000}")
    private int batchSize;

    @Value("${sql.tr.batch_max_delay:100}")
    private long maxDelay;

    @Value("${sql.tr.stats_print_interval_ms:1000}")
    private long statsPrintIntervalMs;

    @Value("${sql.tr.batch_threads:4}")
    private int batchThreads;

    @Value("${sql.batch_sort:false}")
    private boolean batchSortEnabled;

    private TbSqlBlockingQueueWrapper<TelemetryRecognition> queue;

    @PostConstruct
    private void init() {
        TbSqlBlockingQueueParams params = TbSqlBlockingQueueParams.builder()
                .logName("TelemetryRecognition")
                .batchSize(batchSize)
                .maxDelay(maxDelay)
                .statsPrintIntervalMs(statsPrintIntervalMs)
                .statsNamePrefix("telemetryRecognition")
                .batchSortEnabled(batchSortEnabled)
                .build();

        Function<TelemetryRecognition, Integer> hashcodeFunction = entity -> entity.getId().hashCode();
        queue = new TbSqlBlockingQueueWrapper<>(params, hashcodeFunction, batchThreads, statsFactory);
        queue.init(logExecutor, this::saveOrUpdate,
                Comparator.comparing(TelemetryRecognition::getTs).thenComparing(TelemetryRecognition::getDeviceId)
                        .thenComparing(TelemetryRecognition::getAbility)
        );
    }

    @PreDestroy
    private void destroy() {
        if (queue != null) {
            queue.destroy();
        }
    }

    public void saveAddQueue(TelemetryRecognition telemetryRecognition) {
        addToQueue(telemetryRecognition);
    }

    private void addToQueue(TelemetryRecognition entity) {
        Futures.transform(queue.add(entity), v -> null, MoreExecutors.directExecutor());
    }

    private static final String BATCH_INSERT = "INSERT INTO telemetry_recognition(id, created_time, updated_time, device_id, ts, ability, ability_type, value, extra_info)"
            + " VALUES (?::uuid,?,?,?::uuid,?,?,?,?,?) "
            + " ON CONFLICT (device_id, ts, ability) "
            + " DO UPDATE SET created_time = ?, updated_time = ?, ability_type = ?, value = ?, extra_info = ?, id = ?::uuid;";

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void saveOrUpdate(List<TelemetryRecognition> entities) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                int[] result = jdbcTemplate.batchUpdate(BATCH_INSERT, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TelemetryRecognition recognition = entities.get(i);
                        ps.setString(1, recognition.getId().toString());

                        ps.setLong(2, recognition.getCreatedTime());
                        ps.setLong(3, recognition.getUpdatedTime());
                        ps.setString(4, recognition.getDeviceId().toString());
                        ps.setLong(5, recognition.getTs());
                        ps.setString(6, recognition.getAbility());
                        ps.setShort(7, recognition.getAbilityType());
                        ps.setString(8, recognition.getValue());
                        ps.setString(9, recognition.getExtraInfo());

                        // update
                        ps.setLong(10, recognition.getCreatedTime());
                        ps.setLong(11, recognition.getUpdatedTime());
                        ps.setShort(12, recognition.getAbilityType());
                        ps.setString(13, recognition.getValue());
                        ps.setString(14, recognition.getExtraInfo());
                        ps.setString(15, recognition.getId().toString());
                    }

                    @Override
                    public int getBatchSize() {
                        return entities.size();
                    }
                });
            }
        });
    }

}
