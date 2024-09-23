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

package org.thingsboard.rule.engine.customize.lowbattery;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * 低电量-检测
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "Low battery",
        relationTypes = {"True", "False"},
        configClazz = TbLowBatteryNodeConfiguration.class,
        nodeDescription = "一旦所选设备推送的电量信息于所配置的值",
        nodeDetails = "一旦所选设备推送的电量信息于所配置的值,则执行所选的动作(推送数据给接收方或展示在仪表盘组件上)"
)
public class TbLowBatteryNode implements TbNode {
    private TbLowBatteryNodeConfiguration config;

    /**
     * 初始化,主要获取用户配置的数据
     *
     * @param ctx           前规则链节点和上下文信息的属性,常用的方法和属性
     * @param configuration 低电量-节点数据处理配置
     * @throws TbNodeException 规则链节点异常
     */
    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbLowBatteryNodeConfiguration.class);
    }

    /**
     * 中处理业务逻辑
     *
     * @param ctx 前规则链节点和上下文信息的属性,常用的方法和属性
     * @param msg 消息对象
     */
    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException {
        // 包含有关消息来源和目标的元数据信息,例如设备ID、客户端ID、时间戳等
        Integer alarmThreshold = config.getThreshold();
        log.debug("Low battery alarmThreshold = {}", alarmThreshold);

        try {
            EntityId originator = msg.getOriginator();
            // 是否设备属性
            if (!originator.getEntityType().equals(EntityType.DEVICE)) {
                log.info("Low battery Not DEVICE type!ts = {}, entityId = {}", msg.getTs(), originator);
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
                log.debug("Low battery contains False!");
                ctx.tellNext(msg, "False");
                return;
            }
            // 获取电量
            JsonNode jsonNode = JacksonUtil.toJsonNode(msg.getData());
            JsonNode batteryJsonNode = jsonNode.get("battery");
            if (batteryJsonNode == null) {
                log.debug("Low battery not has battery!");
                ctx.tellNext(msg, "False");
                return;
            }
            int battery = batteryJsonNode.intValue();
            log.info("LowBattery Action Info:[{}]The alert threshold is {} and the current threshold is {}", originator, alarmThreshold, battery);
            // 判断下一个节点关系
            boolean next = (battery == 0 && Objects.nonNull(alarmThreshold)) || battery <= alarmThreshold;
            ObjectNode process = PerDataProcessUtil.process(msg, originator);
            // 插入电量
            process.set("battery", batteryJsonNode);
            process.put("alarmThreshold", alarmThreshold);
            msg.setData(process.toString());

            ctx.tellNext(msg, next ? "True" : "False");
            // 保存仪表板数据
            showOnDashboard(ctx, msg, alarmThreshold, battery, next);
        } catch (Exception e) {
            log.warn("[{}] LowBattery Action, Failed to parse message: {}, error message:", ctx.getTenantId(), msg.getData(), e);
            ctx.tellFailure(msg, e);
        }
    }

    private void showOnDashboard(TbContext ctx, TbMsg msg, Integer alarmThreshold, int battery, boolean next) throws IOException {
        if (next && config.isShowOnWidget()) {
            DashboardRuleDevices dashboardRuleDevices = new DashboardRuleDevices();
            dashboardRuleDevices.setType(RuleTriggerEnum.LOW_BATTERY.getValue());
            dashboardRuleDevices.setRuleChainId(new RuleChainOwnId(UUID.fromString(config.getRuleChainOwnId())));
            dashboardRuleDevices.setRuleName(config.getRuleChainName());

            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("battery", battery);
            valueMap.put("alarmThreshold", alarmThreshold);
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
}
