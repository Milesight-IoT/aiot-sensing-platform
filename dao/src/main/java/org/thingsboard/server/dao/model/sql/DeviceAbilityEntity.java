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
import org.thingsboard.server.common.data.DeviceAbility;
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
//@TypeDefs({
//        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
//})
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(name = ModelConstants.DEVICE_ABILITY_TABLE)
public final class DeviceAbilityEntity extends BaseSqlEntity<DeviceAbility> {

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

//    @Type(type = "jsonb")
//    @Column(name = ModelConstants.DEVICE_ABILITIES_ABILITY_COLUMN, columnDefinition = "jsonb")
//    private Abilities abilities;

    public DeviceAbilityEntity(DeviceAbility deviceAbility) {
        this.setUuid(deviceAbility.getId());
        this.setCreatedTime(deviceAbility.getCreatedTime());
        this.setDeviceId(deviceAbility.getDeviceId());
        this.setAbility(deviceAbility.getAbility());
        this.setAbilityType(deviceAbility.getAbilityType());
        this.setAttributes(deviceAbility.getAttributes());
        this.setExtraInfo(deviceAbility.getExtraInfo());
        this.setSensingObjectId(deviceAbility.getSensingObjectId());
    }

    @Override
    public DeviceAbility toData() {
        return DeviceAbility.builder()
                .id(id)
                .createdTime(createdTime)
                .deviceId(deviceId)
                .ability(ability)
                .abilityType(abilityType)
                .attributes(attributes)
                .extraInfo(extraInfo)
                .sensingObjectId(sensingObjectId)
                .build();
    }
}
