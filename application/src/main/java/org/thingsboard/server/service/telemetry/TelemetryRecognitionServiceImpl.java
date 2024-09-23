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

package org.thingsboard.server.service.telemetry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.common.data.recognition.TelemetryRecognitionReceipt;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.session.SessionMsgType;
import org.thingsboard.server.dao.BaseDaoServiceImpl;
import org.thingsboard.server.dao.device.DeviceAbilityService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.rule.RuleChainAssociateService;
import org.thingsboard.server.dao.timeseries.TelemetryRecognitionDao;
import org.thingsboard.server.dao.timeseries.TelemetryRecognitionService;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.queue.TbQueueCallback;
import org.thingsboard.server.queue.TbQueueMsgMetadata;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 感知数据 - 服务实现
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/8/9 9:12
 */
@Service
@Slf4j
@TbCoreComponent
public class TelemetryRecognitionServiceImpl extends BaseDaoServiceImpl<TelemetryRecognitionDao, TelemetryRecognition> implements TelemetryRecognitionService {

    @Autowired
    private DeviceAbilityService deviceAbilityService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private TimeseriesService timeseriesService;
    @Autowired
    protected TbClusterService tbClusterService;
    @Autowired
    protected RuleChainAssociateService ruleChainAssociateService;

    @Override
    public PageData<TelemetryRecognition> findTelemetryRecognition(UUID deviceId, String ability, long startTs, long endTs, PageLink pageLink) {
        return baseDao.findTelemetryRecognition(deviceId, ability, startTs, endTs, pageLink);
    }

    @Override
    public TelemetryRecognition saveTelemetryRecognition(TelemetryRecognition telemetryRecognition) {
        if (telemetryRecognition.getId() != null) {
            telemetryRecognition.setUpdatedTime(System.currentTimeMillis());
        }
        return baseDao.save(null, telemetryRecognition);
    }

    @Override
    public void deleteByDeviceId(UUID deviceId) {
        baseDao.deleteByDeviceId(deviceId);
    }

    @Override
    public Boolean saveForeignTelemetryRecognition(
            List<TelemetryRecognitionReceipt> telemetryRecognitionList)
            throws ThingsboardException, ExecutionException, InterruptedException {
        return checkTheParametersAndSave(telemetryRecognitionList);
    }

    private Boolean checkTheParametersAndSave(List<TelemetryRecognitionReceipt> telemetryRecognitionList)
            throws ThingsboardException, ExecutionException, InterruptedException {
        if (CollectionUtils.isEmpty(telemetryRecognitionList)) {
            log.error("Invalid parameters telemetryRecognitionList can not null ");
            throw new ThingsboardException("Invalid parameters telemetryRecognitionList can not null ", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        AtomicReference<UUID> deviceUUID = new AtomicReference<>();
        AtomicReference<Long> ts = new AtomicReference<>();
        List<TelemetryRecognition> telemetryRecognitions = new ArrayList<>();
        // 保存属性
        telemetryRecognitionList.forEach(receipt -> {
            String id = receipt.getId();
            UUID uuid = UUID.fromString(id);

            try {
                TelemetryRecognition recognition;
                try {
                    recognition = findByIdOrThrow(null, uuid);
                } catch (ThingsboardException e) {
                    log.error("Invalid parameters telemetryRecognitionList can not null !");
                    throw new ThingsboardException("Invalid parameters telemetryRecognitionData does not exist id = " + uuid,
                            ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                ObjectMapper objectMapper = JacksonUtil.OBJECT_MAPPER;
                Map<String, Object> valueMap = objectMapper.readValue(recognition.getValue(), Map.class);
                Optional<String> firstKey = valueMap.keySet().stream().findFirst();
                if (firstKey.isEmpty()) {
                    return;
                }
                valueMap.put(firstKey.get(), receipt.getValue());
                recognition.setValue(objectMapper.writeValueAsString(valueMap));
                // 保存
                saveTelemetryRecognition(recognition);

                // 重置value
                recognition.setValue(receipt.getValue());
                // 参数
                deviceUUID.set(recognition.getDeviceId());
                ts.set(recognition.getTs());
                telemetryRecognitions.add(recognition);
            } catch (JsonProcessingException | ThingsboardException e) {
                log.error("parameter parsing exception! id = {},value = {}", receipt.getId(), receipt.getValue());
            }
        });

        // TODO 发送到第三方
        sendToThirdParty(telemetryRecognitions, deviceUUID, ts);
        return true;
    }

    private void sendToThirdParty(List<TelemetryRecognition> telemetryRecognitionList, AtomicReference<UUID> deviceUUID, AtomicReference<Long> ts) throws ExecutionException, InterruptedException {
        // 校验是否roi通道
        DeviceId deviceId = new DeviceId(deviceUUID.get());
        Device deviceById = deviceService.findDeviceById(TenantId.SYS_TENANT_ID, deviceId);
        Long tsLong = ts.get();
        if (Objects.isNull(deviceById) || Objects.isNull(tsLong)) {
            log.error("device not found! deviceId = {} ,ts = {}", deviceId, tsLong);
            return;
        }
        TenantId tenantId = deviceById.getTenantId();
        // 查找第三方数据
        ListenableFuture<List<TsKvEntry>> latest = timeseriesService.findLatest(tenantId, deviceId, List.of("devName", "devMac"));
        String imageBase64 = timeseriesService.findTelemetryImage(deviceUUID.toString(), tsLong, "image", EntityType.DEVICE.name(), tenantId);
        if (StringUtils.isBlank(imageBase64)) {
            log.error("Send to third party fail,image is not find! deviceName = {},ts = {}", deviceById.getName(), tsLong);
            return;
        }
        List<TsKvEntry> tsKvEntries = latest.get();

        telemetryRecognitionList.forEach(recognition -> initAndSendRuleEngine(tenantId, recognition, imageBase64, deviceById, tsKvEntries));
    }

    private void initAndSendRuleEngine(TenantId tenantId, TelemetryRecognition recognition, String imageBase64, Device device, List<TsKvEntry> tsKvEntries) {
        DeviceId deviceId = device.getId();

        // 查找需要发送的第三方
        ObjectNode objectNode = JacksonUtil.newObjectNode();
        objectNode.put("image", imageBase64);
        objectNode.put("deviceName", device.getName());
        objectNode.put("deviceType", device.getType());
        objectNode.put("roiId", recognition.getId().toString());
        objectNode.put("ts", recognition.getTs());
        objectNode.put("value", recognition.getValue());
        String ability = recognition.getAbility();
        // 获取ROI信息
        DeviceAbility deviceAbility = deviceAbilityService.findDeviceAbilityByDeviceIdAndAbility(deviceId.getId(), ability);
        if (deviceAbility == null) {
            log.error("DeviceAbility is not found deviceName = {}, ability = {}", device.getName(), ability);
            return;
        }
        objectNode.put("ability", ability);
        objectNode.put("coordinate", deviceAbility.getExtraInfo());

        tsKvEntries.forEach(tsKvEntry -> objectNode.put(tsKvEntry.getKey(), tsKvEntry.getValue().toString()));

        TbMsgMetaData metaData = new TbMsgMetaData();
        metaData.putValue("deviceName", device.getName());
        metaData.putValue("deviceType", device.getType());
        metaData.putValue("ts", String.valueOf(System.currentTimeMillis()));
        List<String> recipientsIds = ruleChainAssociateService.findRecipientsByRoi(deviceAbility.getId());
        // 查找需要发送的第三方
        recipientsIds.forEach(recipientsId -> {
            metaData.putValue("recipientsId", recipientsId);
            sendToRoiRuleEngine(tenantId, recognition, device, deviceId, objectNode, metaData);
        });
    }

    private void sendToRoiRuleEngine(TenantId tenantId, TelemetryRecognition recognition, Device device, DeviceId deviceId, ObjectNode objectNode, TbMsgMetaData metaData) {

        TbMsg tbMsg = TbMsg.newMsg(SessionMsgType.MS_ROI_PASSAGE.name(), deviceId, metaData, objectNode.toString());

        // 发送到规则链。自定义网络节点
        tbClusterService.pushMsgToRuleEngine(tenantId, deviceId, tbMsg, new TbQueueCallback() {
            @Override
            public void onSuccess(TbQueueMsgMetadata metadata) {
                log.info("send to roi rule engine success roi = {} , deviceName = {}", recognition.getAbility(), device.getName());
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("send to roi rule engine fail roi = {} , deviceName = {}", recognition.getAbility(), device.getName());
                log.error("send to roi rule engine fail :", t);
            }
        });
    }
}
