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
package org.thingsboard.server.service.entitiy.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.SystemConstant;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.TenantProfile;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.tenant.TbTenantProfileCache;
import org.thingsboard.server.dao.tenant.TenantProfileService;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.AbstractTbEntityService;
import org.thingsboard.server.service.entitiy.queue.TbQueueService;
import org.thingsboard.server.service.install.InstallScripts;
import org.thingsboard.server.service.sync.vc.EntitiesVersionControlService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@TbCoreComponent
@RequiredArgsConstructor
public class DefaultTbTenantService extends AbstractTbEntityService implements TbTenantService {

    private final TenantService tenantService;
    private final TbTenantProfileCache tenantProfileCache;
    private final InstallScripts installScripts;
    private final TbQueueService tbQueueService;
    private final TenantProfileService tenantProfileService;
    private final EntitiesVersionControlService versionControlService;
    private final DashboardService dashboardService;
    private final DeviceProfileService deviceProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant save(Tenant tenant) throws Exception {
        boolean created = tenant.getId() == null;
        Tenant oldTenant = !created ? tenantService.findTenantById(tenant.getId()) : null;

        Tenant savedTenant = checkNotNull(tenantService.saveTenant(tenant));
        if (created) {
            // 初始化规则链
            TenantId tenantId = savedTenant.getId();
            installScripts.createDefaultRuleChains(tenantId);
            installScripts.createDefaultEdgeRuleChains(tenantId);
            // 新增时候加一个初始化仪表板
            addedDefaultDashboard(tenantId);
            // device profile
            deviceProfileService.findOrCreateDeviceProfile(tenantId, "SC541");
            deviceProfileService.findOrCreateDeviceProfile(tenantId, "SC311");
            deviceProfileService.findOrCreateDeviceProfile(tenantId, "SC211");
        }
        tenantProfileCache.evict(savedTenant.getId());
        notificationEntityService.notifyCreateOrUpdateTenant(savedTenant, created ?
                ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);

        TenantProfile oldTenantProfile = oldTenant != null ? tenantProfileService.findTenantProfileById(TenantId.SYS_TENANT_ID, oldTenant.getTenantProfileId()) : null;
        TenantProfile newTenantProfile = tenantProfileService.findTenantProfileById(TenantId.SYS_TENANT_ID, savedTenant.getTenantProfileId());
        tbQueueService.updateQueuesByTenants(Collections.singletonList(savedTenant.getTenantId()), newTenantProfile, oldTenantProfile);
        return savedTenant;
    }

    private void addedDefaultDashboard(TenantId tenantId) {
        // 增加一个默认仪表盘
        Dashboard dashboard = new Dashboard();
        // 时间设置9999年默认最开头
        dashboard.setCreatedTime(SystemConstant.MAX_TIMESTAMP);
        JsonNode jsonNode = JacksonUtil.toJsonNode("{\"description\":\"\"}");
        dashboard.setConfiguration(jsonNode);
        dashboard.setTitle("Default");
        dashboard.setMobileHide(false);
        dashboard.setTenantId(tenantId);
        dashboardService.saveDashboard(dashboard);
    }

    @Override
    public void delete(Tenant tenant) throws Exception {
        TenantId tenantId = tenant.getId();
        tenantService.deleteTenant(tenantId);
        tenantProfileCache.evict(tenantId);
        notificationEntityService.notifyDeleteTenant(tenant);
        versionControlService.deleteVersionControlSettings(tenantId).get(1, TimeUnit.MINUTES);
    }
}
