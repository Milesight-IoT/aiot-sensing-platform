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

package org.thingsboard.server.dao.dashboard;


import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.cache.ms.MsAlarmLastUpdateCache;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.id.DashboardRuleDevicesId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.service.Validator;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.thingsboard.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class DashboardRuleDevicesServiceImpl extends AbstractEntityService implements DashboardRuleDevicesService {
    public static final String INCORRECT_DASHBOARD_RULE_DEVICES_ID = "Incorrect dashboardRuleDevicesId ";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Autowired
    private DashboardRuleDevicesDao dashboardRuleDevicesDao;
    @Autowired
    private MsAlarmLastUpdateCache msAlarmLastUpdateCache;

    @Override
    public DashboardRuleDevices findDashboardRuleDevicesById(TenantId tenantId, DashboardRuleDevicesId dashboardRuleDevicesId) {
        log.trace("Executing findDashboardRuleDevicesById [{}]", dashboardRuleDevicesId);
        Validator.validateId(dashboardRuleDevicesId, INCORRECT_DASHBOARD_RULE_DEVICES_ID + dashboardRuleDevicesId);
        return dashboardRuleDevicesDao.findById(tenantId, dashboardRuleDevicesId.getId());
    }

    @Override
    public ListenableFuture<DashboardRuleDevices> findDashboardRuleDevicesByIdAsync(TenantId tenantId, DashboardRuleDevicesId dashboardRuleDevicesId) {
        log.trace("Executing findDashboardRuleDevicesById [{}]", dashboardRuleDevicesId);
        validateId(dashboardRuleDevicesId, INCORRECT_DASHBOARD_RULE_DEVICES_ID + dashboardRuleDevicesId);
        return dashboardRuleDevicesDao.findByIdAsync(tenantId, dashboardRuleDevicesId.getId());
    }

    @Override
    public PageData<DashboardRuleDevices> findDashboardRuleDevicesByTenantIdAndCreatedTime(TenantId tenantId, Long time, PageLink pageLink) {
        log.trace("Executing findDashboardRuleDevicesByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        Validator.validateId(tenantId, INCORRECT_DASHBOARD_RULE_DEVICES_ID + tenantId);
        Validator.validatePageLink(pageLink);
        return dashboardRuleDevicesDao.findDashboardRuleDevicesByTenantIdAndCreatedTime(tenantId.getId(), time, pageLink);
    }

    @Override
    public CompletableFuture<Void> syncSaveDashboardRuleDevices(DashboardRuleDevices dashboardRuleDevices) {
        return CompletableFuture.runAsync(() -> {
            // 异步执行的代码块
            try {
                saveDashboardRuleDevices(dashboardRuleDevices);
            } catch (IOException e) {
                log.error("Error notice show DashboardRuleDevicesId = {}", dashboardRuleDevices.getId());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public DashboardRuleDevices saveDashboardRuleDevices(DashboardRuleDevices dashboardRuleDevices) throws IOException {
        log.trace("Executing saveDashboardRuleDevices [{}]", dashboardRuleDevices);
        try {
            long createdTime = System.currentTimeMillis();
            if (dashboardRuleDevices.getCreatedTime() == 0) {
                dashboardRuleDevices.setCreatedTime(createdTime);
            }
            DashboardRuleDevices save = dashboardRuleDevicesDao.save(dashboardRuleDevices.getTenantId(), dashboardRuleDevices);
            // 更新状态
            TenantId tenantId = dashboardRuleDevices.getTenantId();
            msAlarmLastUpdateCache.put(tenantId);
            return save;
        } catch (Exception e) {
            checkConstraintViolation(e, "DashboardRuleDevices_external_id_unq_key", "DashboardRuleDevices with such external id already exists!");
            throw e;
        }
    }

}
