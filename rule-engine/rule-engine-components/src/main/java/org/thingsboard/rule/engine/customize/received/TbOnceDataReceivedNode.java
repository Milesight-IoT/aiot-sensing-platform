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

package org.thingsboard.rule.engine.customize.received;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.recognition.TelemetryExtData;
import org.thingsboard.server.common.data.recognition.TelemetryRecognitionThird;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 感知对象的通道接收到数据监听节点
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "Once data received",
        relationTypes = {"True", "False"},
        configClazz = TbOnceDataReceivedNodeConfiguration.class,
        nodeDescription = "感知对象的通道接收到数据监听节点",
        nodeDetails = "一旦所选感知对象的通道接收到数据,就对配置的接收方以JSON格式发送该数据"
)
public class TbOnceDataReceivedNode implements TbNode {
    private TbOnceDataReceivedNodeConfiguration config;

    /**
     * 初始化,主要获取用户配置的数据
     *
     * @param ctx           前规则链节点和上下文信息的属性,常用的方法和属性
     * @param configuration 节点数据处理配置
     * @throws TbNodeException 规则链节点异常
     */
    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbOnceDataReceivedNodeConfiguration.class);
        // getDeviceAbilities(ctx, config.getSensingObjectIds());
    }

    /**
     * 处理业务逻辑
     *
     * @param ctx 前规则链节点和上下文信息的属性,常用的方法和属性
     * @param msg 消息对象
     */
    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException, JsonProcessingException {
        // 包含有关消息来源和目标的元数据信息,例如设备ID、客户端ID、时间戳等
        EntityId originator = msg.getOriginator();
        if (!originator.getEntityType().equals(EntityType.DEVICE)) {
            log.info("Once data received Not DEVICE type!ts = {}, entityId = {}", msg.getTs(), originator);
            ctx.tellNext(msg, "False");
            return;
        }
        // 是否感知能力
        String sensingObjectIds = config.getSensingObjectIds();
        if (StringUtils.isBlank(sensingObjectIds)) {
            // 没有感知对象的时候
            log.info("sensing object ids is null ,to False !ts = {}, metaData = {} ", msg.getTs(),  msg.getMetaData());
            ctx.tellNext(msg, "False");
            return;
        }
        List<String> sensingObjectIdList = Arrays.stream(sensingObjectIds.split(",")).collect(Collectors.toList());
        List<SensingObject> cacheSensingObjectByIds = ctx.getSensingObjectService().findCacheByIds(sensingObjectIdList);
        if (CollectionUtils.isEmpty(cacheSensingObjectByIds)) {
            // 查找不到感知对象说明被删除了
            log.info("sensing object ids is delete ,to False !ts = {}, metaData = {} ", msg.getTs(),  msg.getMetaData());
            ctx.tellNext(msg, "False");
            return;
        }
        // 构造节点
        String deviceId = originator.getId().toString();
        // 能力
        List<String> abilityList = getAbilityList(ctx, sensingObjectIds, deviceId);
        if (CollectionUtils.isEmpty(abilityList)) {
            ctx.tellNext(msg, "False");
            return;
        }
        // 判断是否推送和处理(过滤感知)数据
        boolean isPerceive = isPerceiveAndDataProcessing(msg, abilityList, originator);
        // 去到下一个节点
        String relationType = isPerceive ? "True" : "False";
        log.info("once data received node process is perceive : {} , metaData = {}", relationType, msg.getMetaData());
        ctx.tellNext(msg, relationType);
    }

    private static boolean isPerceiveAndDataProcessing(TbMsg msg, List<String> abilityList, EntityId originator) {
        boolean isPerceive = false;
        JsonNode jsonNode = JacksonUtil.toJsonNode(msg.getData());
        TelemetryExtData telemetryExtData
                = JacksonUtil.FAIL_ON_UNKNOWN_PROPERTIES_MAPPER.convertValue(jsonNode.get("telemetryExtData"), TelemetryExtData.class);
        // 过滤遥测数据
        ObjectNode newObjectNode = JacksonUtil.newObjectNode();
        if (Objects.isNull(telemetryExtData)) {
            Map<String, Object> dataMap = new HashMap<>(msg.getMetaData().getData());
            dataMap.put("deviceId", originator.getId().toString());
            telemetryExtData = TelemetryExtData.builder().deviceInfo(dataMap).build();
        } else {
            // 过滤ROI判断
            List<TelemetryRecognitionThird> telemetryRecognitionThirds = telemetryExtData.getTelemetryRecognitionThirds();
            List<TelemetryRecognitionThird> roiRecognitionThirds = new ArrayList<>();
            for (TelemetryRecognitionThird telemetryRecognitionThird : telemetryRecognitionThirds) {
                String ability = telemetryRecognitionThird.getAbility();
                if (abilityList.contains(ability)) {
                    roiRecognitionThirds.add(telemetryRecognitionThird);
                    isPerceive = true;
                }
            }
            telemetryExtData.setTelemetryRecognitionThirds(roiRecognitionThirds);
            // ROI 添加全图
            if (isPerceive) {
                newObjectNode.set("image", jsonNode.get("image"));
            }
        }

        // 添加基础能力
        for (String ability : abilityList) {
            boolean has = jsonNode.has(ability);
            if (has) {
                newObjectNode.set(ability, jsonNode.get(ability));
                isPerceive = true;
            }
        }
        newObjectNode.set("telemetryExtData", JacksonUtil.valueToTree(telemetryExtData));
        msg.setData(newObjectNode.toString());
        return isPerceive;
    }

    /**
     * 根据感知通道拿到该能力列表
     */
    private List<String> getAbilityList(TbContext ctx, String sensingObjectIds, String deviceId) {
        List<DeviceAbility> deviceAbilities = getDeviceAbilities(ctx, sensingObjectIds);
        if (deviceAbilities == null) {
            return Collections.emptyList();
        }
        // 拼接TbOnceDataReceivedNodeConfiguration 参数
        return deviceAbilities.stream()
                .filter(deviceAbility -> deviceAbility.getDeviceId().toString().equals(deviceId))
                .map(DeviceAbility::getAbility).collect(Collectors.toList());
    }

    @Nullable
    private static List<DeviceAbility> getDeviceAbilities(TbContext ctx, String sensingObjectIds) {
        if (StringUtils.isEmpty(sensingObjectIds)) {
            return Collections.emptyList();
        }
        List<String> abilityIdsByIds = ctx.getSensingObjectService()
                .findAbilityIdsBySensingObjectIds(StringUtils.split(sensingObjectIds, StringUtils.COMMA));
        return ctx.getDeviceAbilityService().findCacheByIds(abilityIdsByIds);
    }
}
