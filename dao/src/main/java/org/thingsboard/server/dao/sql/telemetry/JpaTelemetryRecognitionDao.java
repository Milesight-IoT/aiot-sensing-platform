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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.TelemetryRecognitionEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;
import org.thingsboard.server.dao.timeseries.TelemetryRecognitionDao;

import java.util.List;
import java.util.UUID;

/**
 * 感知数据存储
 */
@Slf4j
@Component
public class JpaTelemetryRecognitionDao extends JpaAbstractDao<TelemetryRecognitionEntity, TelemetryRecognition> implements TelemetryRecognitionDao {

    @Autowired
    private TelemetryRecognitionRepository telemetryRecognitionRepository;

    @Override
    public List<TelemetryRecognitionEntity> findOldTelemetryRecognition(long startTs, int limit) {
        return telemetryRecognitionRepository.findOldTelemetryRecognition(startTs, limit);
    }

    @Override
    public List<TelemetryRecognitionEntity> findOldTelemetryRecognitionByTs(long startTs, long endTs, int limit) {
        return telemetryRecognitionRepository.findOldTelemetryRecognitionByTs(startTs, endTs, limit);
    }

    @Override
    public Long findMinTs() {
        return telemetryRecognitionRepository.findMinTs();
    }

    public void deleteByTsAndDeviceId(long ts, UUID deviceId) {
        telemetryRecognitionRepository.deleteByTsAndDeviceId(ts, deviceId);
    }

    @Override
    public PageData<TelemetryRecognition> findTelemetryRecognition(UUID deviceId, String ability, long startTs, long endTs, PageLink pageLink) {
        return DaoUtil.toPageData(
                telemetryRecognitionRepository.findTelemetryRecognition(
                        deviceId, ability, startTs, endTs, DaoUtil.toPageable(pageLink)));
    }

    @Override
    public int countImageTotal() {
        return telemetryRecognitionRepository.countImageTotal();
    }

    @Override
    public void deleteByDeviceId(UUID deviceId) {
        telemetryRecognitionRepository.deleteByDeviceId(deviceId);
    }

    @Override
    protected Class<TelemetryRecognitionEntity> getEntityClass() {
        return TelemetryRecognitionEntity.class;
    }

    @Override
    protected JpaRepository<TelemetryRecognitionEntity, UUID> getRepository() {
        return telemetryRecognitionRepository;
    }
}
