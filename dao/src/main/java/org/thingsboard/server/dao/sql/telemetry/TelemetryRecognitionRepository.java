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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sql.TelemetryRecognitionEntity;

import java.util.List;
import java.util.UUID;

public interface TelemetryRecognitionRepository extends JpaRepository<TelemetryRecognitionEntity, UUID> {

    @Query(value = "select * from telemetry_recognition where device_id = :deviceId and ability = :ability and ts >= :startTs and ts < :endTs", nativeQuery = true)
    Page<TelemetryRecognitionEntity> findTelemetryRecognition(
            @Param("deviceId") UUID deviceId,
            @Param("ability") String ability,
            @Param("startTs") long startTs,
            @Param("endTs") long endTs,
            Pageable pageable);

    @Query(value = "select * from telemetry_recognition tr where tr.ability = 'image' and ability_type=2 and ts >= :startTs " +
            " order by tr.ts asc limit :limitNum", nativeQuery = true)
    List<TelemetryRecognitionEntity> findOldTelemetryRecognition(@Param("startTs") long startTs,
                                                                 @Param("limitNum") int limitNum);

    @Query(value = "select * from telemetry_recognition tr where tr.ability = 'image' " +
            "and ability_type=2 and ts >= :startTs and ts < :endTs order by tr.ts asc limit :limitNum", nativeQuery = true)
    List<TelemetryRecognitionEntity> findOldTelemetryRecognitionByTs(@Param("startTs") long startTs,
                                                                     @Param("endTs") long endTs,
                                                                     @Param("limitNum") int limitNum);

    @Query(value = "select min(ts) from telemetry_recognition", nativeQuery = true)
    Long findMinTs();

    @Transactional
    void deleteByDeviceId(UUID deviceId);

    /**
     * 删除感知数据
     *
     * @param ts       timestamp
     * @param deviceId 设备ID
     */
    @Transactional
    void deleteByTsAndDeviceId(long ts, UUID deviceId);

    /**
     * 获取图片数据总数
     *
     * @return 图片数据总数
     */
    @Query(value = "select count(*) from telemetry_recognition tr where tr.ability = 'image' and ability_type = 2 ", nativeQuery = true)
    int countImageTotal();
}