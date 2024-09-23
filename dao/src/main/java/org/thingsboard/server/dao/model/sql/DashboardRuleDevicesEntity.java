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

package org.thingsboard.server.dao.model.sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.DashboardRuleDevicesId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.RuleChainOwnId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.BaseEntity;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * 仪表板-规则链触发展示
 *
 * @author zhangzy
 * @date 2023/4/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.DASHBOARD_RULE_DEVICES_TABLE)
public class DashboardRuleDevicesEntity extends BaseSqlEntity<DashboardRuleDevices> implements BaseEntity<DashboardRuleDevices> {

    /**
     * 触发类型
     */
    @Column(name = ModelConstants.DASHBOARD_RULE_DEVICES_TYPE_COLUMN)
    private String type;
    /**
     * 触发值
     */
    @Column(name = ModelConstants.DASHBOARD_RULE_DEVICES_VALUE_COLUMN)
    private String value;
    /**
     * 设备ID
     */
    @Column(name = ModelConstants.DEVICE_ID_PROPERTY)
    private UUID deviceId;
    /**
     * 规则名称
     */
    @Column(name = ModelConstants.DASHBOARD_RULE_DEVICES_RULE_NAME_COLUMN)
    private String ruleName;
    /**
     * 规则ID
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_RULE_CHAIN_ID_COLUMN)
    private UUID ruleChainId;

    @Column(name = ModelConstants.TENANT_ID_COLUMN)
    private UUID tenantId;

    public DashboardRuleDevicesEntity(DashboardRuleDevices dashboardRuleDevices) {
        if (dashboardRuleDevices.getId() != null) {
            this.setUuid(dashboardRuleDevices.getId().getId());
        }
        this.createdTime = dashboardRuleDevices.getCreatedTime();
        this.type = dashboardRuleDevices.getType();
        this.value = dashboardRuleDevices.getValue();
        if (dashboardRuleDevices.getTenantId() != null) {
            this.tenantId = dashboardRuleDevices.getTenantId().getId();
        }
        if (dashboardRuleDevices.getDeviceId() != null) {
            this.deviceId = dashboardRuleDevices.getDeviceId().getId();
        }
        if (dashboardRuleDevices.getRuleChainId() != null) {
            this.ruleChainId = dashboardRuleDevices.getRuleChainId().getId();
        }
        this.ruleName = dashboardRuleDevices.getRuleName();

    }

    @Override
    public DashboardRuleDevices toData() {
        DashboardRuleDevices ruleDevices = new DashboardRuleDevices(new DashboardRuleDevicesId(this.getUuid()));
        ruleDevices.setCreatedTime(createdTime);
        ruleDevices.setTenantId(TenantId.fromUUID(tenantId));
        ruleDevices.setType(type);
        ruleDevices.setValue(value);
        ruleDevices.setDeviceId(new DeviceId(deviceId));
        if (!StringUtils.isEmpty(ruleName)) {
            ruleDevices.setRuleName(ruleName);
        }
        if (ruleChainId != null) {
            ruleDevices.setRuleChainId(new RuleChainOwnId(ruleChainId));
        }
        return ruleDevices;
    }
}
