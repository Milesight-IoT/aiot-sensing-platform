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

import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.model.sql.TelemetryRecognitionEntity;

import java.util.List;
import java.util.UUID;

public interface TelemetryRecognitionDao extends Dao<TelemetryRecognition> {

    List<TelemetryRecognitionEntity> findOldTelemetryRecognition(long startTs, int limit);

    List<TelemetryRecognitionEntity> findOldTelemetryRecognitionByTs(long startTs, long endTs, int limit);

    Long findMinTs();

    void deleteByTsAndDeviceId(long ts, UUID deviceId);

    PageData<TelemetryRecognition> findTelemetryRecognition(UUID device, String ability, long startTs, long endTs, PageLink pageLink);

    void deleteByDeviceId(UUID deviceId);

    int countImageTotal();

}
