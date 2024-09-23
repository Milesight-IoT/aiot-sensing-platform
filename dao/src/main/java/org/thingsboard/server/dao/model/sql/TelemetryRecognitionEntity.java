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
package org.thingsboard.server.dao.model.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = ModelConstants.TELEMETRY_RECOGNITION_TABLE)
public final class TelemetryRecognitionEntity extends BaseSqlEntity<TelemetryRecognition> {

    @Column(name = ModelConstants.UPDATED_TIME_PROPERTY)
    private long updatedTime;

    @Column(name = ModelConstants.DEVICE_ID_PROPERTY)
    private UUID deviceId;

    @Column(name = ModelConstants.TELEMETRY_RECOGNITION_TS_COLUMN)
    private long ts;

    @Column(name = ModelConstants.TELEMETRY_RECOGNITION_ABILITY_COLUMN)
    private String ability;

    @Column(name = ModelConstants.TELEMETRY_RECOGNITION_TYPE_COLUMN)
    private short abilityType;

    @Column(name = ModelConstants.TELEMETRY_RECOGNITION_VALUE_COLUMN)
    private String value;

    @Column(name = ModelConstants.TELEMETRY_RECOGNITION_EXTRA_INFO_COLUMN)
    private String extraInfo;

    public TelemetryRecognitionEntity(TelemetryRecognition telemetryRecognition) {
        this.setUuid(telemetryRecognition.getId());
        this.setCreatedTime(telemetryRecognition.getCreatedTime());
        this.setUpdatedTime(telemetryRecognition.getUpdatedTime());
        this.setDeviceId(telemetryRecognition.getDeviceId());
        this.setTs(telemetryRecognition.getTs());
        this.setAbility(telemetryRecognition.getAbility());
        this.setAbilityType(telemetryRecognition.getAbilityType());
        this.setValue(telemetryRecognition.getValue());
        this.setExtraInfo(telemetryRecognition.getExtraInfo());
    }

    @Override
    public TelemetryRecognition toData() {
        return TelemetryRecognition.builder()
                .id(id)
                .createdTime(createdTime)
                .updatedTime(updatedTime)
                .deviceId(deviceId)
                .ts(ts)
                .ability(ability)
                .abilityType(abilityType)
                .value(value)
                .extraInfo(extraInfo)
                .build();
    }
}
