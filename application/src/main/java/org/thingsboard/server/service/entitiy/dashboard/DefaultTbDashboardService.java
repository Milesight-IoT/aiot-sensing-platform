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
package org.thingsboard.server.service.entitiy.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.WidgetsUtil;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.ShortCustomerInfo;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.customWidget.WidgetDTO;
import org.thingsboard.server.common.data.edge.Edge;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DashboardId;
import org.thingsboard.server.common.data.id.EdgeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.AbstractTbEntityService;
import org.thingsboard.server.service.install.InstallScripts;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@TbCoreComponent
@AllArgsConstructor
@Slf4j
public class DefaultTbDashboardService extends AbstractTbEntityService implements TbDashboardService {


    private final String PATH_WIDGETS = "widgets";
    private final String PATH_DEFAULT = "default";
    private final String PATH_STATES = "states";
    private final String PATH_LAYOUTS = "layouts";
    private final String PATH_MAIN = "main";

    private final String FIX_BASE_CONFIG = "{\"description\": \"\", \"widgets\": {}}";

    private final DashboardService dashboardService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private InstallScripts installScripts;

    @Override
    public boolean checkDashboardName(TenantId tenantId, String dashboardName) {
        // 判断仪表盘是否存在
        List<Dashboard> alreadyDash = dashboardService.findTenantDashboardsByTitle(tenantId, dashboardName);
        boolean result = alreadyDash.size() > 0;
        return result;
    }

    @Override
    public Dashboard findByCreatedTime(UUID tenantId, long time) {
        return dashboardService.findByCreatedTime(tenantId, time);
    }

    @Override
    public Dashboard save(Dashboard dashboard, User user) throws Exception {
        ActionType actionType = dashboard.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        TenantId tenantId = dashboard.getTenantId();
        try {
            Dashboard savedDashboard = checkNotNull(dashboardService.saveDashboard(dashboard));
            autoCommit(user, savedDashboard.getId());
            // 向系统发送有关实体创建或更新事件的通知消息
            notificationEntityService.notifyCreateOrUpdateEntity(tenantId, savedDashboard.getId(), savedDashboard,
                    null, actionType, user);
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), dashboard, actionType, user, e);
            throw e;
        }
    }

    @Override
    public void delete(Dashboard dashboard, User user) {
        DashboardId dashboardId = dashboard.getId();
        TenantId tenantId = dashboard.getTenantId();
        try {
            List<EdgeId> relatedEdgeIds = edgeService.findAllRelatedEdgeIds(tenantId, dashboardId);
            dashboardService.deleteDashboard(tenantId, dashboardId);
            notificationEntityService.notifyDeleteEntity(tenantId, dashboardId, dashboard, null,
                    ActionType.DELETED, relatedEdgeIds, user, dashboardId.toString());
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), ActionType.DELETED, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard assignDashboardToCustomer(Dashboard dashboard, Customer customer, User user) throws ThingsboardException {
        ActionType actionType = ActionType.ASSIGNED_TO_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        CustomerId customerId = customer.getId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Dashboard savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(tenantId, dashboardId, customerId));
            notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, customerId, savedDashboard,
                    actionType, user, dashboardId.toString(), customerId.toString(), customer.getName());
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType,
                    user, e, dashboardId.toString(), customerId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard assignDashboardToPublicCustomer(Dashboard dashboard, User user) throws ThingsboardException {
        ActionType actionType = ActionType.ASSIGNED_TO_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(tenantId);
            Dashboard savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(tenantId, dashboardId, publicCustomer.getId()));
            notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, publicCustomer.getId(), savedDashboard,
                    actionType, user, dashboardId.toString(),
                    publicCustomer.getId().toString(), publicCustomer.getName());
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard unassignDashboardFromPublicCustomer(Dashboard dashboard, User user) throws ThingsboardException {
        ActionType actionType = ActionType.UNASSIGNED_FROM_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(tenantId);
            Dashboard savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(tenantId, dashboardId, publicCustomer.getId()));
            notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, publicCustomer.getId(), dashboard,
                    actionType, user, dashboardId.toString(),
                    publicCustomer.getId().toString(), publicCustomer.getName());
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard updateDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException {
        ActionType actionType = ActionType.ASSIGNED_TO_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Set<CustomerId> addedCustomerIds = new HashSet<>();
            Set<CustomerId> removedCustomerIds = new HashSet<>();
            for (CustomerId customerId : customerIds) {
                if (!dashboard.isAssignedToCustomer(customerId)) {
                    addedCustomerIds.add(customerId);
                }
            }

            Set<ShortCustomerInfo> assignedCustomers = dashboard.getAssignedCustomers();
            if (assignedCustomers != null) {
                for (ShortCustomerInfo customerInfo : assignedCustomers) {
                    if (!customerIds.contains(customerInfo.getCustomerId())) {
                        removedCustomerIds.add(customerInfo.getCustomerId());
                    }
                }
            }

            if (addedCustomerIds.isEmpty() && removedCustomerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : addedCustomerIds) {
                    savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(tenantId, dashboardId, customerId));
                    ShortCustomerInfo customerInfo = savedDashboard.getAssignedCustomerInfo(customerId);
                    notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, savedDashboard.getId(), customerId, savedDashboard,
                            actionType, user, dashboardId.toString(), customerId.toString(), customerInfo.getTitle());
                }
                actionType = ActionType.UNASSIGNED_FROM_CUSTOMER;
                for (CustomerId customerId : removedCustomerIds) {
                    ShortCustomerInfo customerInfo = dashboard.getAssignedCustomerInfo(customerId);
                    savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(tenantId, dashboardId, customerId));
                    notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, savedDashboard.getId(), customerId, savedDashboard,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, user, dashboardId.toString(), customerId.toString(), customerInfo.getTitle());
                }
                return savedDashboard;
            }
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard addDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException {
        ActionType actionType = ActionType.ASSIGNED_TO_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Set<CustomerId> addedCustomerIds = new HashSet<>();
            for (CustomerId customerId : customerIds) {
                if (!dashboard.isAssignedToCustomer(customerId)) {
                    addedCustomerIds.add(customerId);
                }
            }
            if (addedCustomerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : addedCustomerIds) {
                    savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(tenantId, dashboardId, customerId));
                    ShortCustomerInfo customerInfo = savedDashboard.getAssignedCustomerInfo(customerId);
                    notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, customerId, savedDashboard,
                            actionType, user, dashboardId.toString(), customerId.toString(), customerInfo.getTitle());
                }
                return savedDashboard;
            }
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard removeDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException {
        ActionType actionType = ActionType.UNASSIGNED_FROM_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Set<CustomerId> removedCustomerIds = new HashSet<>();
            for (CustomerId customerId : customerIds) {
                if (dashboard.isAssignedToCustomer(customerId)) {
                    removedCustomerIds.add(customerId);
                }
            }
            if (removedCustomerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : removedCustomerIds) {
                    ShortCustomerInfo customerInfo = dashboard.getAssignedCustomerInfo(customerId);
                    savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(tenantId, dashboardId, customerId));
                    notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, customerId, savedDashboard,
                            actionType, user, dashboardId.toString(), customerId.toString(), customerInfo.getTitle());
                }
                return savedDashboard;
            }
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard asignDashboardToEdge(TenantId tenantId, DashboardId dashboardId, Edge edge, User user) throws ThingsboardException {
        ActionType actionType = ActionType.ASSIGNED_TO_EDGE;
        EdgeId edgeId = edge.getId();
        try {
            Dashboard savedDashboard = checkNotNull(dashboardService.assignDashboardToEdge(tenantId, dashboardId, edgeId));
            notificationEntityService.notifyAssignOrUnassignEntityToEdge(tenantId, dashboardId, null,
                    edgeId, savedDashboard, actionType, user, dashboardId.toString(),
                    edgeId.toString(), edge.getName());
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DEVICE),
                    actionType, user, e, dashboardId.toString(), edgeId);
            throw e;
        }
    }

    @Override
    public Dashboard unassignDashboardFromEdge(Dashboard dashboard, Edge edge, User user) throws ThingsboardException {
        ActionType actionType = ActionType.UNASSIGNED_FROM_EDGE;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        EdgeId edgeId = edge.getId();
        try {
            Dashboard savedDevice = checkNotNull(dashboardService.unassignDashboardFromEdge(tenantId, dashboardId, edgeId));

            notificationEntityService.notifyAssignOrUnassignEntityToEdge(tenantId, dashboardId, null,
                    edgeId, dashboard, actionType, user, dashboardId.toString(),
                    edgeId.toString(), edge.getName());
            return savedDevice;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e,
                    dashboardId.toString(), edgeId.toString());
            throw e;
        }
    }

    @Override
    public Dashboard unassignDashboardFromCustomer(Dashboard dashboard, Customer customer, User user) throws ThingsboardException {
        ActionType actionType = ActionType.UNASSIGNED_FROM_CUSTOMER;
        TenantId tenantId = dashboard.getTenantId();
        DashboardId dashboardId = dashboard.getId();
        try {
            Dashboard savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(tenantId, dashboardId, customer.getId()));
            notificationEntityService.notifyAssignOrUnassignEntityToCustomer(tenantId, dashboardId, customer.getId(), savedDashboard,
                    actionType, user, dashboardId.toString(), customer.getId().toString(), customer.getName());
            return savedDashboard;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.DASHBOARD), actionType, user, e, dashboardId.toString());
            throw e;
        }
    }

    /**
     * 参数转换成对象
     */
    @Override
    public Dashboard paramTransDashboard(String dashboardName, String dashboardId, SecurityUser user, TenantId tenantId) throws ThingsboardException {
        Dashboard dashboard = new Dashboard();
        Optional<String> optional = Optional.ofNullable(dashboardId);

        nameVerification(dashboardName, dashboardId, tenantId);

        // 仪表板id判断是否为空,为非空返回转UUID类型
        if (optional.isPresent()) {
            DashboardId id = new DashboardId(UUID.fromString(optional.get()));
            dashboard = dashboardService.findDashboardById(tenantId, id);
            if (dashboard == null) {
                log.error("Dashboard not found , does not exist! id ={}", id);
                throw new ThingsboardException("data not found , does not exist!", ThingsboardErrorCode.GENERAL);
            }
        } else {
            JsonNode node = JacksonUtil.OBJECT_MAPPER.createObjectNode().put("description", "");
            dashboard.setConfiguration(node);
            dashboard.setCreatedTime(System.currentTimeMillis());
        }
        dashboard.setTenantId(tenantId);
        dashboard.setTitle(dashboardName);
        return dashboard;
    }

    private void nameVerification(String dashboardName, String dashboardId, TenantId tenantId) throws ThingsboardException {
        // 校验名称是否存在
        boolean duplicateName = dashboardService.hasByTenantIdAndIdOrName(tenantId, dashboardId, dashboardName);
        if(duplicateName){
            log.error("Dashboard is exist! id ={} ,dashboardName = {}", dashboardId, dashboardName);
            throw new ThingsboardException("Dashboard already exists!", ThingsboardErrorCode.GENERAL);
        }
    }

    @Override
    public Dashboard createDashboardWidget(Dashboard dashboard, List<WidgetDTO> dto) throws RuntimeException, JsonProcessingException {
        // 所有部件map key：uuid ,value：部件配置
        Map<String, Map> widgetsMap = new HashMap();
        dto.forEach(widgetDTO -> {
            UUID uuid = UUID.randomUUID();
            Map<String, Object> widgetMap = WidgetsUtil.fixedDashboardWidget(widgetDTO, uuid);
            widgetsMap.put(uuid.toString(), widgetMap);
        });
        // map转jsonNode
        JsonNode jsonNode = JacksonUtil.toJsonNode(JacksonUtil.OBJECT_MAPPER.writeValueAsString(widgetsMap));
        JsonNode baseConfigurationNode = JacksonUtil.toJsonNode(FIX_BASE_CONFIG);
        ((ObjectNode) baseConfigurationNode).set(PATH_WIDGETS, jsonNode);
        dashboard.setConfiguration(baseConfigurationNode);
        return dashboard;
    }

    /**
     * 第一版设计,参照原仪表板设计,后端生成配置,能够在原仪表板展示
     * 目前不需要,前端直接固定控件,后期调整再使用,目前保留
     *
     * @param dashboard
     * @param dto
     * @return
     * @throws IOException
     * @throws RuntimeException
     */
    @Override
    public Dashboard createDashboardOwnWidget(Dashboard dashboard, List<WidgetDTO> dto) throws IOException, RuntimeException {
        // 读json文件,生成基础配置
        JsonNode baseConfigurationNode = installScripts.createBaseDashboardConfiguration();
        // 所有部件map key：uuid ,value：部件配置
        Map<String, Map> widgetsMap = new HashMap();
        // 所有部件位置大小map key：uuid ,value：部件配置
        Map<String, Map> layoutsMap = new HashMap<>();
        dto.stream().forEach(widgetDTO -> {
            UUID uuid = UUID.randomUUID();
            Map<String, Object> widgetMap = new HashMap<>();
            Map<String, Object> layoutMap = new HashMap<>();
            switch (widgetDTO.getType()) {
                case "Active devices":
                    widgetMap = WidgetsUtil.createActiveDevice(widgetDTO, uuid);
                    layoutMap = WidgetsUtil.dashboardWidgetsLayout(widgetDTO);
                    break;
                case "Inactive devices":
                    widgetMap = WidgetsUtil.createInActiveDevice(widgetDTO, uuid);
                    layoutMap = WidgetsUtil.dashboardWidgetsLayout(widgetDTO);
                    break;
                case "Total devices":
                    widgetMap = WidgetsUtil.createTotalDevice(widgetDTO, uuid);
                    layoutMap = WidgetsUtil.dashboardWidgetsLayout(widgetDTO);
                    break;
                case "Snapshot preview":
                    widgetMap = WidgetsUtil.createSnapshotPreview(widgetDTO, uuid);
                    layoutMap = WidgetsUtil.dashboardWidgetsLayout(widgetDTO);
                    break;
                case "Alarm table":
                    widgetMap = WidgetsUtil.createAlarmTable(widgetDTO, uuid);
                    layoutMap = WidgetsUtil.dashboardWidgetsLayout(widgetDTO);
                default:
                    break;
            }
            layoutsMap.put(uuid.toString(), layoutMap);
            widgetsMap.put(uuid.toString(), widgetMap);
        });
        JsonNode jsonNode = JacksonUtil.toJsonNode(JacksonUtil.OBJECT_MAPPER.writeValueAsString(widgetsMap));
        JsonNode layoutNode = JacksonUtil.toJsonNode(JacksonUtil.OBJECT_MAPPER.writeValueAsString(layoutsMap));
        ((ObjectNode) baseConfigurationNode.get(PATH_STATES).get(PATH_DEFAULT).get(PATH_LAYOUTS).get(PATH_MAIN)).put(PATH_WIDGETS, layoutNode);
        ((ObjectNode) baseConfigurationNode).put(PATH_WIDGETS, jsonNode);
        dashboard.setConfiguration(baseConfigurationNode);
        return dashboard;
    }

}
