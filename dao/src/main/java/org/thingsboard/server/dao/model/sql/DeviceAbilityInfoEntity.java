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
import org.thingsboard.server.common.data.DeviceAbilityInfo;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = ModelConstants.DEVICE_ABILITY_TABLE)
public final class DeviceAbilityInfoEntity extends BaseSqlEntity<DeviceAbilityInfo> {

    @Column(name = ModelConstants.DEVICE_ID_PROPERTY)
    private UUID deviceId;

    @Column(name = ModelConstants.DEVICE_ABILITY_ABILITY_COLUMN)
    private String ability;

    @Column(name = ModelConstants.DEVICE_ABILITY_ABILITY_TYPE_COLUMN)
    private short abilityType;

    @Column(name = ModelConstants.DEVICE_ABILITY_ATTRIBUTES_COLUMN)
    private String attributes;

    @Column(name = ModelConstants.DEVICE_ABILITY_EXTRA_INFO_COLUMN)
    private String extraInfo;

    @Column(name = ModelConstants.DEVICE_ABILITY_SENSING_OBJECT_ID_COLUMN)
    private UUID sensingObjectId;

    @Transient
    private String deviceName;

    @Transient
    private Long ts;

    @Transient
    private String value;

    public DeviceAbilityInfoEntity(
            DeviceAbilityInfoEntity deviceAbilityInfoEntity,
            String deviceName) {
        this.setUuid(deviceAbilityInfoEntity.getId());
        this.setCreatedTime(deviceAbilityInfoEntity.getCreatedTime());
        this.setDeviceId(deviceAbilityInfoEntity.getDeviceId());
        this.setAbility(deviceAbilityInfoEntity.getAbility());
        this.setAbilityType(deviceAbilityInfoEntity.getAbilityType());
        this.setAttributes(deviceAbilityInfoEntity.getAttributes());
        this.setExtraInfo(deviceAbilityInfoEntity.getExtraInfo());
        this.setDeviceName(deviceName);
    }

    @Override
    public DeviceAbilityInfo toData() {
        return DeviceAbilityInfo.builder()
                .deviceAbilityId(id)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ability(ability)
                .abilityType(abilityType)
                .extraInfo(extraInfo)
                .ts(ts)
                .value(value)
                .build();
    }
}
