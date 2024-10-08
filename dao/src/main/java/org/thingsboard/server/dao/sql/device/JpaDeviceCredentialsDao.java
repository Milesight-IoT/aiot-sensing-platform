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
package org.thingsboard.server.dao.sql.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.device.DeviceCredentialsDao;
import org.thingsboard.server.dao.model.sql.DeviceCredentialsEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
@SqlDao
public class JpaDeviceCredentialsDao extends JpaAbstractDao<DeviceCredentialsEntity, DeviceCredentials> implements DeviceCredentialsDao {

    @Autowired
    private DeviceCredentialsRepository deviceCredentialsRepository;

    @Override
    protected Class<DeviceCredentialsEntity> getEntityClass() {
        return DeviceCredentialsEntity.class;
    }

    @Override
    protected JpaRepository<DeviceCredentialsEntity, UUID> getRepository() {
        return deviceCredentialsRepository;
    }

    @Transactional
    @Override
    public DeviceCredentials saveAndFlush(TenantId tenantId, DeviceCredentials deviceCredentials) {
        DeviceCredentials result = save(tenantId, deviceCredentials);
        deviceCredentialsRepository.flush();
        return result;
    }

    @Override
    public DeviceCredentials findByDeviceId(TenantId tenantId, UUID deviceId) {
        return DaoUtil.getData(deviceCredentialsRepository.findByDeviceId(deviceId));
    }

    @Override
    public DeviceCredentials findByCredentialsId(TenantId tenantId, String credentialsId) {
        return DaoUtil.getData(deviceCredentialsRepository.findByCredentialsId(credentialsId));
    }
}
