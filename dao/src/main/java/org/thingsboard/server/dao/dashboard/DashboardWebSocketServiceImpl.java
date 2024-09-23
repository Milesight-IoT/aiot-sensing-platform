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


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.cache.ms.MsAlarmLastUpdateCache;
import org.thingsboard.server.cache.ms.MsDeviceLastUpdateCache;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.ms.MsAlarmDataQuery;
import org.thingsboard.server.dao.device.DeviceAbilityService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.sensing.SensingObjectService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author zhangzy
 * data 2023-04-19
 */
@Service
@Slf4j
public class DashboardWebSocketServiceImpl implements DashboardWebSocketService {
    @Autowired
    private MsDeviceLastUpdateCache deviceLastUpdateCache;
    @Autowired
    private MsAlarmLastUpdateCache msAlarmLastUpdateCache;
    @Autowired
    private DashboardRuleDevicesService dashboardRuleDevicesService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceAbilityService deviceAbilityService;
    @Autowired
    private SensingObjectService sensingObjectService;

    @Override
    public Optional<PageData<DashboardRuleDevices>> sendWsMsAlarmDataMessage(MsAlarmDataQuery query, TenantId tenantId) {
        boolean isContainsKey = msAlarmLastUpdateCache.containsKey(tenantId);
        if (!isContainsKey) {
            return Optional.empty();
        }
        PageData<DashboardRuleDevices> pageData = dashboardRuleDevicesService.
                findDashboardRuleDevicesByTenantIdAndCreatedTime(tenantId, query.getStartTime(), query.getPageLink());
        List<DashboardRuleDevices> dashboardRuleDevices = pageData.getData();
        dashboardRuleDevices.forEach(obj -> {
            Device device = deviceService.findDeviceById(tenantId, obj.getDeviceId());
            obj.setSource(device.getName());
        });
        return Optional.of(pageData);
    }

    @Override
    public Optional<TsKvEntry> sendWsSnapshotPreviewMessage(boolean isGet, TenantId tenantId, DeviceId deviceId) {
        if (!isGet) {
            boolean isContainsKey = deviceLastUpdateCache.containsKey(deviceId.getId().toString());
            Device deviceById = deviceService.findDeviceById(tenantId, deviceId);
            if (deviceById == null || !isContainsKey) {
                return Optional.empty();
            }
        }
        // UUID需要装换成String类型判断
        TsKvEntry tsKvEnTry = sensingObjectService.getTsKvEnTry(tenantId, deviceId);
        if (tsKvEnTry == null) {
            return Optional.empty();
        }
        return Optional.of(tsKvEnTry);
    }

    @Override
    public Optional<DeviceAbility> getRoiAbility(String abilityId) {
        if (StringUtils.isBlank(abilityId)) {
            log.debug("No ability id = {}", abilityId);
            return Optional.empty();
        }
        DeviceAbility deviceAbility = deviceAbilityService.getDeviceAbilityByIdCache(UUID.fromString(abilityId));
        if (deviceAbility == null) {
            return Optional.empty();
        }
        return Optional.of(deviceAbility);
    }

    @Override
    public Optional<SensingObject> getSensingObjectById(String sensingObjectId) {
        if (StringUtils.isBlank(sensingObjectId)) {
            log.debug("No sensingObjectId id = {}", sensingObjectId);
            return Optional.empty();
        }
        SensingObject sensingObjectById = sensingObjectService.getSensingObjectById(UUID.fromString(sensingObjectId));
        if (sensingObjectById == null) {
            return Optional.empty();
        }
        return Optional.of(sensingObjectById);
    }
}
