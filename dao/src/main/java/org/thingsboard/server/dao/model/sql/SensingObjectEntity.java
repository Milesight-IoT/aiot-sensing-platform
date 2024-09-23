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
import org.thingsboard.server.common.data.SensingObject;
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
@Table(name = ModelConstants.SENSING_OBJECT_TABLE)
public final class SensingObjectEntity extends BaseSqlEntity<SensingObject> {

    @Column(name = ModelConstants.TENANT_ID_COLUMN)
    private UUID tenantId;

    @Column(name = ModelConstants.SENSING_OBJECT_NAME_COLUMN)
    private String name;

    @Column(name = ModelConstants.SENSING_OBJECT_DEVICE_ABILITY_IDS_COLUMN)
    private String deviceAbilityIds;

    public SensingObjectEntity(SensingObject sensingObject) {
        this.setUuid(sensingObject.getId());
        this.setCreatedTime(sensingObject.getCreatedTime());
        this.setTenantId(sensingObject.getTenantId());
        this.setName(sensingObject.getName());
        this.setDeviceAbilityIds(sensingObject.getDeviceAbilityIds());
    }

    @Override
    public SensingObject toData() {
        return SensingObject.builder()
                .id(id)
                .createdTime(createdTime)
                .tenantId(tenantId)
                .name(name)
                .deviceAbilityIds(deviceAbilityIds)
                .build();
    }
}
