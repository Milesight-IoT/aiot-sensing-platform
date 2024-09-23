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

package org.thingsboard.rule.engine.customize.activestatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.rule.engine.customize.PerDataProcessUtil;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.RuleChainOwnId;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.enume.rule.RuleTriggerEnum;
import org.thingsboard.server.common.msg.TbMsg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * 活跃状态变为不活跃监听节点
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "Devices become inactive",
        relationTypes = {"True", "False"},
        configClazz = TbActiveStatusNodeConfiguration.class,
        nodeDescription = "活跃状态变为不活跃监听节点",
        nodeDetails = "所选设备从活跃状态变为不活跃,则执行所选动作(推送数据给接收方或展示在仪表盘组件上)"
)
public class TbActiveStatusNode implements TbNode {
    private TbActiveStatusNodeConfiguration config;


    /**
     * 初始化,主要获取用户配置的数据
     *
     * @param ctx           前规则链节点和上下文信息的属性,常用的方法和属性
     * @param configuration 节点数据处理配置
     * @throws TbNodeException 规则链节点异常
     */
    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbActiveStatusNodeConfiguration.class);
    }

    /**
     * 处理业务逻辑
     *
     * @param ctx 前规则链节点和上下文信息的属性,常用的方法和属性
     * @param msg 消息对象
     */
    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException, IOException {
        // 包含有关消息来源和目标的元数据信息,例如设备ID、客户端ID、时间戳等
        EntityId originator = msg.getOriginator();
        if (!originator.getEntityType().equals(EntityType.DEVICE)) {
            log.debug("Devices become inactive not DEVICE type!ts = {}, entityId = {}", msg.getTs(), originator);
            ctx.tellNext(msg, "False");
            return;
        }
        String deviceIds = config.getDeviceIds();
        // 这个节点是否选择设备
        if (StringUtils.isBlank(deviceIds)
                // 是否在这个规则
                || !deviceIds.contains(originator.getId().toString())
                // 校验设备是否存在
                || ctx.getDeviceService().findDeviceById(ctx.getTenantId(), new DeviceId(originator.getId())) == null) {
            log.debug("Devices become inactive contains False! deviceIds = {}, originator = {}", deviceIds, originator);
            ctx.tellNext(msg, "False");
            return;
        }
        log.info("Devices become inactive Info:[{}] to inactive", originator);
        // 设置推送数据
        ObjectNode process = PerDataProcessUtil.processActiveMsg(msg, originator);
        process.put("activeStatus", false);
        msg.setData(process.toString());

        ctx.tellNext(msg, "True");
        // 保存仪表板数据
        showOnDashboard(ctx, msg);
    }


    private void showOnDashboard(TbContext ctx, TbMsg msg) throws IOException {
        if (!config.isShowOnWidget()) {
            return;
        }
        DashboardRuleDevices dashboardRuleDevices = new DashboardRuleDevices();
        dashboardRuleDevices.setType(RuleTriggerEnum.DEVICES_BECOME_INACTIVE.getValue());
        dashboardRuleDevices.setRuleChainId(new RuleChainOwnId(UUID.fromString(config.getRuleChainOwnId())));
        dashboardRuleDevices.setRuleName(config.getRuleChainName());

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("activeStatus", false);
        String value = JacksonUtil.OBJECT_MAPPER.writeValueAsString(valueMap);
        dashboardRuleDevices.setValue(value);

        dashboardRuleDevices.setTenantId(ctx.getTenantId());
        DeviceId deviceId = new DeviceId(msg.getOriginator().getId());
        dashboardRuleDevices.setDeviceId(deviceId);
        Device deviceById = ctx.getDeviceService().findDeviceById(ctx.getTenantId(), deviceId);
        if (deviceById != null) {
            dashboardRuleDevices.setSource(deviceById.getName());
        }
        ctx.getDashboardRuleDevicesService().saveDashboardRuleDevices(dashboardRuleDevices);
    }
}
