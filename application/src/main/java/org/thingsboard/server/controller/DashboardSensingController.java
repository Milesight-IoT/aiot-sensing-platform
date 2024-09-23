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

package org.thingsboard.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.DashboardInfo;
import org.thingsboard.server.common.data.SystemConstant;
import org.thingsboard.server.common.data.customWidget.DashboardWidgetDto;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.widget.WidgetsBundle;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.dashboard.TbDashboardService;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 仪表板
 *
 * @author Zhangzy
 * @version 1.0.1.1
 * 2023/4/12 15:30
 */
@RestController
@TbCoreComponent
@RequiredArgsConstructor
@RequestMapping("/api/sensing")
public class DashboardSensingController extends BaseController {

    private final TbDashboardService tbDashboardService;


    /**
     * （MS） 仪表板列表
     *
     * @param pageSize     页数
     * @param page         页码
     * @param textSearch   查询值
     * @param sortProperty 排序字段
     * @param sortOrder    DESC降序或者ASC升序
     * @return {@link PageData<DashboardInfo>}
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/dashboards", method = RequestMethod.GET)
    @ResponseBody
    public PageData<DashboardInfo> getTenantDashboards(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder) throws ThingsboardException {
        TenantId tenantId = getCurrentUser().getTenantId();
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return checkNotNull(dashboardService.findDashboardsByTenantId(tenantId, pageLink));
    }

    /**
     * （MS）仪表板创建或者更新
     *
     * @param dashboardName 仪表板名称
     * @param openPublic    公开 true,私有false
     * @param dashboardId   仪表板id,创建null,更新传id
     * @return {@link Dashboard}
     * @throws Exception 异常
     */
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/dashboard", method = RequestMethod.POST)
    @ResponseBody
    public Dashboard saveDashboard(@RequestParam String dashboardName,
                                   @RequestParam Boolean openPublic,
                                   @RequestParam(required = false) String dashboardId) throws Exception {
        SecurityUser user = getCurrentUser();
        Dashboard dashboard = tbDashboardService.paramTransDashboard(dashboardName, dashboardId, user, getTenantId());
        checkEntity(dashboard.getId(), dashboard, Resource.DASHBOARD);
        // 先保存返回仪表板id再去设置公开,公开链接需要仪表板id
        Dashboard saveDashboard = tbDashboardService.save(dashboard, user);
        if (openPublic == null) {
            return saveDashboard;
        }
        if (openPublic) {
            // 公开
            return tbDashboardService.assignDashboardToPublicCustomer(saveDashboard, user);
        } else {
            // 私有
            return tbDashboardService.unassignDashboardFromPublicCustomer(saveDashboard, user);
        }
    }

    /**
     * （MS）查询部件包
     *
     * @return {@link List<WidgetsBundle>}
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/widgetsBundles", method = RequestMethod.GET)
    @ResponseBody
    public List<WidgetsBundle> getWidgetsBundles() throws ThingsboardException {
        try {
            List<WidgetsBundle> widgetsBundles = checkNotNull(widgetsBundleService.findAllTenantWidgetsBundlesByTenantId(getTenantId()));
            // 目前只有个性化部件包,其他先过滤
            widgetsBundles = widgetsBundles.stream()
                    .filter(bundles -> Objects.equals("Personalise widgets", bundles.getTitle()))
                    .collect(Collectors.toList());
            return widgetsBundles;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * （MS）仪表盘配置保存
     *
     * @param dto dashboard对象+所添加部件
     * @return {@link Dashboard}
     * @throws Exception 异常
     */
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/dashboard/widgets", method = RequestMethod.POST)
    @ResponseBody
    public Dashboard saveDashboardWidgets(@RequestBody DashboardWidgetDto dto) throws Exception {
        Dashboard dashboard = dto.getDashboard();
        dashboard.setTenantId(getTenantId());
        checkEntityId(dashboard.getId(), Operation.WRITE);
        dashboard = tbDashboardService.createDashboardWidget(dashboard, dto.getWidgetDTOList());
        return tbDashboardService.save(dashboard, getCurrentUser());
    }

    /**
     * （MS）查询default仪表板Id
     *
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/getDefaultId", method = RequestMethod.GET)
    @ResponseBody
    public UUID getDefaultId() throws ThingsboardException {
        try {
            Dashboard dashboard = tbDashboardService.findByCreatedTime(getTenantId().getId(), SystemConstant.MAX_TIMESTAMP);
            return dashboard.getId().getId();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * （MS）校验仪表盘名称是否存在
     *
     * @param dashboardName 仪表盘名称
     * @return true已存在
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/checkDashboardName", method = RequestMethod.GET)
    @ResponseBody
    public boolean checkDashboardName(@RequestParam String dashboardName) throws ThingsboardException {
        return tbDashboardService.checkDashboardName(getTenantId(), dashboardName);
    }
}
