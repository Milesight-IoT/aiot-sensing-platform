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

package org.thingsboard.server.controller.foreign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.recognition.TelemetryRecognitionReceipt;
import org.thingsboard.server.dao.rule.RuleChainService;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.timeseries.TelemetryRecognitionService;
import org.thingsboard.server.service.install.InstallScripts;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 对外的遥测数据相关接口 - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/23 10:24
 */
@Slf4j
@RestController
@RequestMapping("/api/foreign/telemetry")
public class ForeignTelemetryController extends BaseForeignControllerController {
    @Autowired
    private TelemetryRecognitionService telemetryRecognitionService;

    @Autowired
    InstallScripts installScripts;
    @Autowired
    RuleChainService ruleChainService;
    @Autowired
    TenantService tenantService;

    /**
     * （MS）遥测数据人工识别（对外）
     *
     * @param telemetryRecognitionList 列表
     * @return Boolean
     */
    @RequestMapping(value = "/recognition", method = RequestMethod.POST)
    public Boolean getRecognition(@Valid @RequestBody List<TelemetryRecognitionReceipt> telemetryRecognitionList)
            throws ThingsboardException, ExecutionException, InterruptedException {
        verifyLogin();
        return telemetryRecognitionService.saveForeignTelemetryRecognition(telemetryRecognitionList);
    }
    //
    // /**
    //  * （MS）初始化脚本测试
    //  */
    // @GetMapping(value = "/initData")
    // public void initData() {
    //     PageData<Tenant> tenants = tenantService.findTenants(new PageLink(30));
    //     tenants.getData().forEach(tenant -> {
    //         if (!MsAppContent.ADMIN_ACCOUNT.equals(tenant.getName())) {
    //             return;
    //         }
    //         try {
    //             ruleChainService.deleteRuleChainsByTenantId(tenant.getId());
    //             installScripts.createDefaultRuleChains(tenant.getId());
    //         } catch (Exception e) {
    //             log.error("Unable to update Tenant", e);
    //         }
    //     });
    // }
    //
    // /**
    //  * （MS）脚本升级测试
    //  */
    // @Transactional(rollbackFor = Exception.class)
    // @GetMapping(value = "/testUpdate")
    // public void updateData() {
    //     PageData<Tenant> tenants = tenantService.findTenants(new PageLink(30));
    //     tenants.getData().forEach(tenant -> {
    //         // if (!MsAppContent.ADMIN_ACCOUNT.equals(tenant.getName())) {
    //         //     return;
    //         // }
    //         installScripts.addOnceResultRuleChain(tenant);
    //     });
    // }

}