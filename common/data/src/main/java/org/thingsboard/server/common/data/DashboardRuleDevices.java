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

package org.thingsboard.server.common.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.thingsboard.server.common.data.id.DashboardRuleDevicesId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.RuleChainOwnId;
import org.thingsboard.server.common.data.id.TenantId;



/**
 * @author zhangzy
 * @data 2023-04-19
 */
@Builder
@Data
@AllArgsConstructor
public class DashboardRuleDevices extends BaseData<DashboardRuleDevicesId> implements HasName, HasTenantId {

    /**
     * 创建时间
     */
    private long createdTime;
    /**
     * 触发类型
     */
    private String type;
    /**
     * 触发值
     */
    private String value;
    /**
     * 设备ID
     */
    private DeviceId deviceId;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则ID
     */
    private RuleChainOwnId ruleChainId;
    /**
     * 租户ID
     */
    private TenantId tenantId;
    /**
     * 来源设备
     */
    private String source;

    public DashboardRuleDevices() {
        super();
    }

    public DashboardRuleDevices(DashboardRuleDevicesId id) {
        super(id);
    }

    public DashboardRuleDevicesId getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return "dashboard_rule_devices";
    }

}
