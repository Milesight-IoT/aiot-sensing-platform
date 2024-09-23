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
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.id.DashboardRuleDevicesId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author Zhangzy
 * @data 2022/4/19
 */
public interface DashboardRuleDevicesService {

    DashboardRuleDevices findDashboardRuleDevicesById(TenantId tenantId, DashboardRuleDevicesId dashboardRuleDevicesId);

    ListenableFuture<DashboardRuleDevices> findDashboardRuleDevicesByIdAsync(TenantId tenantId, DashboardRuleDevicesId dashboardRuleDevicesId);

    /**
     * 根据租户ID分页查询
     *
     * @param tenantId 租户ID
     * @param time     开始时间戳
     * @param pageLink 分页信息
     * @return PageData
     */
    PageData<DashboardRuleDevices> findDashboardRuleDevicesByTenantIdAndCreatedTime(TenantId tenantId, Long time, PageLink pageLink);

    /**
     * 保存MS列表仪表板数据
     *
     * @param dashboardRuleDevices 仪表板告警
     * @return CompletableFuture
     * @throws IOException e
     */
    DashboardRuleDevices saveDashboardRuleDevices(DashboardRuleDevices dashboardRuleDevices) throws IOException;

    /**
     * 异步保存MS列表仪表板数据
     *
     * @param dashboardRuleDevices 仪表板告警
     * @return CompletableFuture
     * @throws IOException e
     */
    CompletableFuture<Void> syncSaveDashboardRuleDevices(DashboardRuleDevices dashboardRuleDevices) throws IOException;
}
