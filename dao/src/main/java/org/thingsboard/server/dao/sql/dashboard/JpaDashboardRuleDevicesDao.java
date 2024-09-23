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

package org.thingsboard.server.dao.sql.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.dashboard.DashboardRuleDevicesDao;
import org.thingsboard.server.dao.model.sql.DashboardRuleDevicesEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.UUID;

/**
 * @author zhangzy
 * @Date 2022/04/19
 */
@Slf4j
@Component
@SqlDao
public class JpaDashboardRuleDevicesDao extends JpaAbstractDao<DashboardRuleDevicesEntity, DashboardRuleDevices> implements DashboardRuleDevicesDao {

    @Autowired
    DashboardRuleDevicesRepository repository;

    @Override
    protected Class<DashboardRuleDevicesEntity> getEntityClass() {
        return DashboardRuleDevicesEntity.class;
    }

    @Override
    protected JpaRepository<DashboardRuleDevicesEntity, UUID> getRepository() {
        return repository;
    }

    @Override
    public PageData<DashboardRuleDevices> findDashboardRuleDevicesByTenantIdAndCreatedTime(UUID tenantId, Long time, PageLink pageLink) {
        return DaoUtil.toPageData(repository.findByTenantIdAndTime(tenantId, time, DaoUtil.toPageable(pageLink)));
    }

}
