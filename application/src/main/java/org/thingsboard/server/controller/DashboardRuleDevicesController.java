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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.dao.dashboard.DashboardRuleDevicesService;

/**
 * 仪表板触发仪表板
 *
 * @author Zhangzy
 * @version 1.0.1.1
 * 2023/4/12 15:30
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardRuleDevicesController extends BaseController {

    @Autowired
    private DashboardRuleDevicesService dashboardRuleDevicesService;

    /**
     * （MS）遥测触发规则保存仪表板的table部件数据
     *
     * @param dashboardRuleDevices 保存对象
     * @return {@link DashboardRuleDevices}
     * @throws Exception 异常
     */
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/dashboardRuleDevices", method = RequestMethod.POST)
    @ResponseBody
    public DashboardRuleDevices saveDashboardRuleDevices(@RequestBody DashboardRuleDevices dashboardRuleDevices) throws Exception {
        DashboardRuleDevices devices = dashboardRuleDevicesService.saveDashboardRuleDevices(dashboardRuleDevices);
        return devices;
    }
}
