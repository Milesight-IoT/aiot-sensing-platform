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

import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.customWidget.WidgetDTO;
import org.thingsboard.server.common.data.edge.Edge;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DashboardId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.service.entitiy.SimpleTbEntityService;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TbDashboardService extends SimpleTbEntityService<Dashboard> {

    boolean checkDashboardName(TenantId tenantId, String dashboardName);

    Dashboard findByCreatedTime(UUID tenantId, long time);

    Dashboard assignDashboardToCustomer(Dashboard dashboard, Customer customer, User user) throws ThingsboardException;

    Dashboard assignDashboardToPublicCustomer(Dashboard dashboard, User user) throws ThingsboardException;

    Dashboard unassignDashboardFromPublicCustomer(Dashboard dashboard, User user) throws ThingsboardException;

    Dashboard updateDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException;

    Dashboard addDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException;

    Dashboard removeDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws ThingsboardException;

    Dashboard asignDashboardToEdge(TenantId tenantId, DashboardId dashboardId, Edge edge, User user) throws ThingsboardException;

    Dashboard unassignDashboardFromEdge(Dashboard dashboard, Edge edge, User user) throws ThingsboardException;

    Dashboard unassignDashboardFromCustomer(Dashboard dashboard, Customer customer, User user) throws ThingsboardException;

    Dashboard paramTransDashboard(String dashboardName, String dashboardId, SecurityUser user, TenantId tenantId) throws ThingsboardException;

    Dashboard createDashboardWidget(Dashboard dashboard, List<WidgetDTO> dto) throws IOException, RuntimeException;

    Dashboard createDashboardOwnWidget(Dashboard dashboard, List<WidgetDTO> dtos) throws IOException;
}
