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

package org.thingsboard.server.dao.rule;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleChainOwnId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.rule.RuleChain;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleChainType;
import org.thingsboard.server.common.data.rule.RuleNode;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;
import org.thingsboard.server.common.data.rule.customize.ActiveStatusNodeData;
import org.thingsboard.server.common.data.rule.customize.BaseCustomizeRuleNodeData;
import org.thingsboard.server.common.data.rule.customize.LowBatteryNodeData;
import org.thingsboard.server.common.data.rule.customize.OnceDataReceivedNodeData;
import org.thingsboard.server.common.data.rule.customize.OnceResultRecognizedNodeData;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.common.data.rule.own.RuleChainOwnDto;
import org.thingsboard.server.common.enume.IEnum;
import org.thingsboard.server.common.enume.rule.RuleActionTypeEnum;
import org.thingsboard.server.common.enume.rule.RuleChainAssociateTypeEnum;
import org.thingsboard.server.common.enume.rule.RuleTriggerEnum;
import org.thingsboard.server.dao.BaseDaoServiceImpl;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.recipients.RecipientsService;
import org.thingsboard.server.dao.relation.RelationService;
import org.thingsboard.server.dao.sensing.SensingObjectService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 规则链表（二开）- 服务实现
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/3 10:59
 */
@Service
@Slf4j
public class RuleChainOwnServiceImpl extends BaseDaoServiceImpl<RuleChainOwnDao, RuleChainOwn> implements RuleChainOwnService {

    @Resource
    private RelationService relationService;
    @Resource
    private SensingObjectService sensingObjectService;
    @Resource
    private BaseRuleChainService baseRuleChainService;
    @Resource
    private RuleChainAssociateService ruleChainAssociateService;
    @Resource
    private RecipientsService recipientsService;
    @Resource
    private DeviceService deviceService;

    @Override
    public PageData<RuleChainOwn> findTenantRuleChainOwnList(TenantId tenantId, PageLink pageLink) {
        PageData<RuleChainOwn> pageData = baseDao.findTenantRuleChainOwnList(tenantId.getId(), pageLink);
        List<RuleChainOwn> data = pageData.getData();
        // data.forEach(ruleChainOwn -> {
        //     String trigger = ruleChainOwn.getTrigger();
        //     Object jsonData = ruleChainOwn.getJsonData();
        //     try {
        //         if (RuleTriggerEnum.ONCE_DATA_RECEIVED.eq(trigger)) {
        //             // 感知对象的通道接收到数据
        //             OnceDataReceivedNodeData onceDataReceivedNodeData = convertValue("", jsonData, OnceDataReceivedNodeData.class);
        //             List<String> keys = filterAwareChannel(onceDataReceivedNodeData);
        //             onceDataReceivedNodeData.setSensingObjectIds(String.join(StringUtils.COMMA, keys));
        //             ruleChainOwn.setJsonData(onceDataReceivedNodeData);
        //         } else if (RuleTriggerEnum.DEVICES_BECOME_INACTIVE.eq(trigger)) {
        //             ActiveStatusNodeData activeStatusNodeData = convertValue("", jsonData, ActiveStatusNodeData.class);
        //             List<String> keys = filterEquipment(tenantId, activeStatusNodeData.getDeviceIds());
        //             activeStatusNodeData.setDeviceIds(String.join(StringUtils.COMMA, keys));
        //             ruleChainOwn.setJsonData(activeStatusNodeData);
        //         } else if (RuleTriggerEnum.LOW_BATTERY.eq(trigger)) {
        //             LowBatteryNodeData lowBatteryNodeData = convertValue("", jsonData, LowBatteryNodeData.class);
        //             List<String> keys = filterEquipment(tenantId, lowBatteryNodeData.getDeviceIds());
        //             lowBatteryNodeData.setDeviceIds(String.join(StringUtils.COMMA, keys));
        //             ruleChainOwn.setJsonData(lowBatteryNodeData);
        //         }
        //     } catch (ThingsboardException e) {
        //         log.error("Failed to convert! id ={}", ruleChainOwn.getRuleChainOwnId());
        //     }
        // });
        return pageData;
    }

    private List<String> filterEquipment(TenantId tenantId, String deviceIds) {
        List<String> keys = new ArrayList<>();
        Arrays.stream(deviceIds.split(StringUtils.COMMA))
                .filter(StringUtils::isNoneBlank).forEach(key -> {
                    DeviceId deviceId = new DeviceId(UUID.fromString(key));
                    Device deviceById = deviceService.findDeviceById(tenantId, deviceId);
                    if (Objects.nonNull(deviceById)) {
                        keys.add(deviceById.getId().toString());
                    }
                });
        return keys;
    }

    private List<String> filterAwareChannel(OnceDataReceivedNodeData onceDataReceivedNodeData) {
        List<String> keys = new ArrayList<>();
        String sensingObjectIds = onceDataReceivedNodeData.getSensingObjectIds();
        Arrays.stream(sensingObjectIds.split(StringUtils.COMMA))
                .filter(StringUtils::isNoneBlank).forEach(key -> {
                    UUID uuid = UUID.fromString(key);
                    SensingObject sensingObjectById = sensingObjectService.getSensingObjectById(uuid);
                    if (Objects.nonNull(sensingObjectById)) {
                        keys.add(sensingObjectById.getId().toString());
                    }
                });
        return keys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleChainOwn createOrUpdate(TenantId tenantId, RuleChainOwnDto dto) throws ThingsboardException {
        // 校验重名
        suggestDuplicateName(tenantId, dto);
        // create or update
        RuleChainOwn entityData = Objects.nonNull(dto.getRuleChainOwnId())
                ? findByIdOrThrow(tenantId, dto.getRuleChainOwnId().getId()) : new RuleChainOwn();
        // 校验属性
        verifyTrigger(dto, entityData);
        // 复制属性
        BeanUtils.copyProperties(dto, entityData, RuleChainOwn.class);
        // 租户ID
        entityData.setTenantId(tenantId.getId());
        // 需要用到关联所以自己设置UUID
        RuleChainOwnId ruleChainOwnId = entityData.getRuleChainOwnId();
        if (ruleChainOwnId == null) {
            UUID uuid = Uuids.timeBased();
            entityData.setRuleChainOwnId(new RuleChainOwnId(uuid));
            entityData.setCreatedTime(Uuids.unixTimestamp(uuid));
        }
        // 修改后更改规则节点配置
        modifyRuleNode(tenantId, entityData);
        log.info("createOrUpdate RuleChainOwn name = {} , id = {}", entityData.getName(), entityData.getRuleChainOwnId());
        return baseDao.save(tenantId, entityData);
    }

    private void suggestDuplicateName(TenantId tenantId, RuleChainOwnDto dto) throws ThingsboardException {
        RuleChainOwn byTenantIdAndName = getByTenantIdAndName(tenantId, dto.getName());
        if (byTenantIdAndName != null && !byTenantIdAndName.getRuleChainOwnId().equals(dto.getRuleChainOwnId())) {
            log.error("Rule chain is exist! id ={} , RuleChainOwnName = {}", dto.getRuleChainOwnId(), dto.getName());
            throw new ThingsboardException("Rule chain already exists!", ThingsboardErrorCode.GENERAL);
        }
    }

    private void verifyTrigger(RuleChainOwnDto dto, RuleChainOwn entityData) throws ThingsboardException {
        String trigger = entityData.getTrigger();
        String dtoTrigger = dto.getTrigger();
        if (StringUtils.isNotBlank(trigger) && !trigger.equals(dtoTrigger)) {
            log.warn("Trigger is not same Can not be modified trigger = {}, dtoTrigger ={}", trigger, dtoTrigger);
            throw new ThingsboardException("Trigger is not same ,please can not be modified !", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    private void modifyRuleNode(TenantId tenantId, RuleChainOwn entityData) throws ThingsboardException {
        RuleTriggerEnum ruleTriggerEnum = IEnum.of(RuleTriggerEnum.class, entityData.getTrigger());
        if (ruleTriggerEnum.getNeedAddNode()) {
            // 初始化主节点 和 二开规则链信息
            RuleNode mainRuleNode = getMainRuleNode(tenantId, entityData);
            // 设置动作
            initAtionsData(tenantId, entityData, mainRuleNode);
            // 设置触发器属性
            initTriggerData(tenantId, entityData, mainRuleNode);
        } else {
            // 删除关系
            UUID id = entityData.getRuleChainOwnId().getId();
            ruleChainAssociateService.deleteByRuleId(id);

            OnceResultRecognizedNodeData nodeData = convertValueUnknownProperties(entityData.getJsonData(), OnceResultRecognizedNodeData.class);

            List<RuleChainAssociate> chainAssociates = new ArrayList<>();
            // 新增关系 RuleChainOwnId + ROI 关联
            String abilityIds = nodeData.getAbilityIds();
            addTriggerAssociate(abilityIds, id, RuleChainAssociateTypeEnum.ROI_RULE_CHAIN_OWN.getValue(), tenantId, chainAssociates);
            // 新增关系 RuleChainOwnId + Recipients 关联
            String recipients = nodeData.getRecipients();
            addTriggerAssociate(recipients, id, RuleChainAssociateTypeEnum.RECIPIENTS_RULE_CHAIN_OWN.getValue(), tenantId, chainAssociates);

            ruleChainAssociateService.saveAll(chainAssociates);
        }
    }

    private RuleNode getMainRuleNode(TenantId tenantId, RuleChainOwn entityData) throws ThingsboardException {
        UUID ruleNodeId = entityData.getRuleNodeId();
        List<EntityRelation> relations = new ArrayList<>();
        RuleNode mainRuleNode = new RuleNode();
        if (Objects.isNull(ruleNodeId)) {
            // 新增节点
            ruleNodeId = Uuids.timeBased();
            entityData.setRuleNodeId(ruleNodeId);
            mainRuleNode.setId(new RuleNodeId(ruleNodeId));
            mainRuleNode.setCreatedTime(Uuids.unixTimestamp(ruleNodeId));
            // 设置规则链ID
            RuleTriggerEnum ruleTriggerEnum = IEnum.of(RuleTriggerEnum.class, entityData.getTrigger());
            String ruleChainName = ruleTriggerEnum.getRuleChainName();
            try {
                Collection<RuleChain> chains
                        = baseRuleChainService.findTenantRuleChainsByTypeAndName(tenantId, RuleChainType.CORE, ruleChainName);
                RuleChain ruleChain = new ArrayList<>(chains).get(0);
                RuleChainId ruleChainId = ruleChain.getId();
                mainRuleNode.setRuleChainId(ruleChainId);
                // 二开规则链设置属性
                entityData.setRuleChainId(ruleChainId.getId());
                // 新增关系
                addEntityRelation(relations, mainRuleNode, ruleTriggerEnum, ruleChain);
            } catch (Exception e) {
                log.error("initialization failed,Root rule chain query exception Name = {} ", ruleChainName, e);
                throw new ThingsboardException("initialization failed," + ruleChainName + "Root rule chain query exception",
                        ThingsboardErrorCode.GENERAL);
            }
            mainRuleNode.setDebugMode(false);
            // 位置信息
            ObjectNode objectNode = getAdditionalInfoJsonNodes(650, 2000);
            mainRuleNode.setAdditionalInfo(objectNode);

            // 保存关系线路
            relationService.saveRelations(tenantId, relations);
        } else {
            // 旧节点
            mainRuleNode = baseRuleChainService.findRuleNodeById(tenantId, new RuleNodeId(ruleNodeId));
            if (Objects.isNull(mainRuleNode)) {
                // 旧节点不存在说明数据问题
                log.error("initialization failed,Root node query exception name ={},id = {}", entityData.getName(), entityData.getRuleChainOwnId());
                throw new ThingsboardException("initialization failed,"
                        + entityData.getName() +
                        " Root node query exception,Please delete the data and add it again!", ThingsboardErrorCode.GENERAL);
            } else {
                // 先删除 (规则节点)
                ruleChainAssociateService.deleteByRuleId(mainRuleNode.getId().getId());
            }
        }
        mainRuleNode.setName(entityData.getName());

        return mainRuleNode;
    }

    private void initAtionsData(TenantId tenantId, RuleChainOwn entityData, RuleNode mainRuleNode) throws ThingsboardException {
        // 构建关系
        BaseCustomizeRuleNodeData ruleNodeData = convertValueUnknownProperties(entityData.getJsonData(), BaseCustomizeRuleNodeData.class);
        String ations = entityData.getActions();
        if (ations.contains(RuleActionTypeEnum.SEND_TO_RECIPIENTS.getValue())) {
            String recipients = ruleNodeData.getRecipients();
            List<UUID> uuidRecipientsList = Arrays.stream(ruleNodeData.getRecipients().split(StringUtils.COMMA))
                    .map(UUID::fromString).collect(Collectors.toList());
            List<Recipients> recipientsList = recipientsService.findByIds(uuidRecipientsList);

            List<String> effectiveRecipientsIdList = new ArrayList<>();
            // 校验第三方主键是否有效
            verifyRecipients(entityData, recipients, recipientsList, effectiveRecipientsIdList);

            // 新增 规则链 HTTP /MQTT 节点
            Map<UUID, Recipients> uuidRecipientsMap = recipientsList.stream()
                    .collect(Collectors.toMap(key -> key.getRecipientsId().getId(), value -> value, (v1, v2) -> v1));
            // 判断该规则链是否需要新增节点
            List<RuleChainAssociate> ruleChainAssociates = new ArrayList<>();

            // 接收方和规则链关系
            newRuleNodeToChain(tenantId, mainRuleNode, entityData, effectiveRecipientsIdList, uuidRecipientsMap, ruleChainAssociates);
            // 处理 接收方 - (二开) 规则节点 直接关系
            dealRecipientsRuleChainOwnRelation(tenantId, entityData, recipientsList, ruleChainAssociates);

            ruleChainAssociateService.saveAll(ruleChainAssociates);
        }
    }

    private void dealRecipientsRuleChainOwnRelation(TenantId tenantId, RuleChainOwn entityData, List<Recipients> recipientsList,
                                                    List<RuleChainAssociate> ruleChainAssociates) {

        // 添加
        String value = RuleChainAssociateTypeEnum.RECIPIENTS_RULE_CHAIN_OWN.getValue();
        UUID ruleChainOwnId = entityData.getRuleChainOwnId().getId();
        recipientsList.forEach(recipientData -> {
            UUID recipientsId = recipientData.getRecipientsId().getId();
            RuleChainAssociate ruleNodeAssociate = getRuleNodeAssociate(ruleChainOwnId, value, tenantId, recipientsId);
            ruleChainAssociates.add(ruleNodeAssociate);
        });
        // 先删除 接收方 - (二开) 规则节点 直接关系
        ruleChainAssociateService.deleteByRuleId(ruleChainOwnId);
    }

    private void newRuleNodeToChain(TenantId tenantId, RuleNode ruleNode, RuleChainOwn entityData,
                                    List<String> effectiveRecipientsIdList, Map<UUID, Recipients> uuidRecipientsMap,
                                    List<RuleChainAssociate> ruleChainAssociates) {
        RuleChainId ruleChainId = ruleNode.getRuleChainId();
        RuleNodeId nodeId = ruleNode.getId();

        List<Recipients> addRecipients = new ArrayList<>();
        List<EntityRelation> relations = new ArrayList<>();
        // 旧的连接属性,已存在的 RuleChainAssociate 属性

        RuleChainAssociateTypeEnum typeEnum = IEnum.labelOf(RuleChainAssociateTypeEnum.class, entityData.getTrigger());
        List<RuleNodeAssociate> oldAssociateList
                = ruleChainAssociateService.findRecipientsByAssociateIdIn(effectiveRecipientsIdList, typeEnum.getValue(), ruleChainId);

        for (String effectiveRecipientsId : effectiveRecipientsIdList) {
            UUID recipientsId = UUID.fromString(effectiveRecipientsId);
            Optional<RuleNodeAssociate> optional = oldAssociateList.stream()
                    .filter(associate -> associate.getAssociateId().equals(recipientsId)).findFirst();
            if (optional.isEmpty()) {
                // 不存在的节点需要新增节点
                addRecipients.add(uuidRecipientsMap.get(recipientsId));
            } else {
                RuleNodeAssociate ruleNodeAssociate = optional.get();

                RuleNodeId ruleNodeId = new RuleNodeId(ruleNodeAssociate.getRuleNodeId());
                EntityRelation ruleNodeRelation =
                        getMainEntityRelation(nodeId, ruleNodeId, EntityRelation.TRUE, RelationTypeGroup.RULE_NODE);
                relations.add(ruleNodeRelation);
            }
        }
        // 删除FROM这个规则节点的连接
        deleteChainRelation(tenantId, ruleNode, relations);

        if (CollectionUtils.isEmpty(addRecipients)) {
            log.info("No need to add new nodes");
            relationService.saveRelations(tenantId, relations);
            return;
        }

        // 新增关系(责任链关系)
        addRecipients.forEach(recipients -> {
            // 初始化接收方规则节点
            RuleNode recipientsNode = initRecipientsRuleNode(ruleChainId, recipients);
            // 新增关系 True关系
            EntityRelation ruleNodeRelation
                    = getMainEntityRelation(nodeId, recipientsNode.getId(), EntityRelation.TRUE, RelationTypeGroup.RULE_NODE);
            relations.add(ruleNodeRelation);
            // 包含关系 Contains
            EntityRelation ruleChainRelation = getMainEntityRelation(ruleChainId, recipientsNode.getId(), EntityRelation.CONTAINS_TYPE,
                    RelationTypeGroup.RULE_CHAIN);
            relations.add(ruleChainRelation);

            // 新增关系(接收人-规则节点)
            RecipientsId recipientsId = recipients.getRecipientsId();
            addRuleNodeAssociate(tenantId, ruleChainId, recipientsNode.getId(), ruleChainAssociates, recipientsId.getId(), typeEnum);

            // 保存节点
            baseRuleChainService.saveRuleNode(tenantId, recipientsNode);
        });

        relationService.saveRelations(tenantId, relations);
    }

    @NotNull
    private RuleNode initRecipientsRuleNode(RuleChainId ruleChainId, Recipients recipients) {
        RuleNode recipientsNode = new RuleNode();
        // 新增节点
        UUID recipientsNodeId = Uuids.timeBased();
        recipientsNode.setId(new RuleNodeId(recipientsNodeId));
        recipientsNode.setCreatedTime(Uuids.unixTimestamp(recipientsNodeId));
        recipientsNode.setRuleChainId(ruleChainId);

        recipientsNode.setName(recipients.getName());
        recipientsNode.setDebugMode(false);
        // 位置信息
        ObjectNode objectNode = getAdditionalInfoJsonNodes(1000, 1000);
        recipientsNode.setAdditionalInfo(objectNode);

        // 设置节点属性
        recipientsService.setConfigurationAndType(recipients, recipientsNode);
        return recipientsNode;
    }

    private void deleteChainRelation(TenantId tenantId, RuleNode ruleNode, List<EntityRelation> relations) {
        List<EntityRelation> fromRelationList = relationService.findByFrom(tenantId, ruleNode.getId());
        fromRelationList.stream()
                .filter(entityRelation -> {
                    // 过滤掉需要保存的节点
                    for (EntityRelation relation : relations) {
                        String newFrom = relation.getFrom().getId().toString();
                        String newTo = relation.getTo().getId().toString();
                        String oldFrom = entityRelation.getFrom().getId().toString();
                        String oldTo = entityRelation.getTo().getId().toString();
                        if (newFrom.equals(oldFrom) && newTo.equals(oldTo)) {
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(entityRelation -> relationService.deleteRelation(tenantId, entityRelation));
    }

    private void addRuleNodeAssociate(TenantId tenantId, RuleChainId ruleChainId, RuleNodeId nodeId,
                                      List<RuleChainAssociate> chainAssociates, UUID recipientsId, RuleChainAssociateTypeEnum typeEnum) {

        String recipientsChainType = RuleChainAssociateTypeEnum.RECIPIENTS_CHAIN.getValue();
        String nodeValueType = typeEnum.getValue();

        RuleChainAssociate oldNodeAssociate = getRuleNodeAssociate(nodeId.getId(), nodeValueType, tenantId, recipientsId);
        chainAssociates.add(oldNodeAssociate);
        RuleChainAssociate oldChainAssociate = getRuleNodeAssociate(ruleChainId.getId(), recipientsChainType, tenantId, recipientsId);
        chainAssociates.add(oldChainAssociate);
    }


    private void initTriggerData(TenantId tenantId, RuleChainOwn entityData, RuleNode ruleNode) throws ThingsboardException {
        List<RuleChainAssociate> ruleChainAssociates = new ArrayList<>();
        // 校验JSONData 并且添加触发器过滤条件关系
        String trigger = entityData.getTrigger();
        Object jsonData = entityData.getJsonData();
        // 是否显示在部件上
        boolean isShowOnWidget = entityData.getActions().contains(RuleActionTypeEnum.SHOW_ON_WIDGET.getValue());
        // 规则节点ID
        UUID ruleNodeId = ruleNode.getId().getId();
        ObjectNode objectNode = JacksonUtil.OBJECT_MAPPER.createObjectNode();
        if (RuleTriggerEnum.ONCE_DATA_RECEIVED.eq(trigger)) {
            OnceDataReceivedNodeData receivedNodeData = convertValue("jsonData", jsonData, OnceDataReceivedNodeData.class);
            String sensingObjectIds = receivedNodeData.getSensingObjectIds();
            // 过滤条件关系
            String type = RuleChainAssociateTypeEnum.SENSING_OBJECT_CHANNEL_NODE.getValue();
            addTriggerAssociate(sensingObjectIds, ruleNodeId, type, tenantId, ruleChainAssociates);

            objectNode.put("sensingObjectIds", sensingObjectIds);

            ruleNode.setType("org.thingsboard.rule.engine.customize.received.TbOnceDataReceivedNode");
        } else if (RuleTriggerEnum.DEVICES_BECOME_INACTIVE.eq(trigger)) {
            ActiveStatusNodeData activeStatusNodeData = convertValue("jsonData", jsonData, ActiveStatusNodeData.class);
            String deviceIds = activeStatusNodeData.getDeviceIds();
            // 过滤条件关系
            String type = RuleChainAssociateTypeEnum.DEVICE_NODE.getValue();
            addTriggerAssociate(deviceIds, ruleNodeId, type, tenantId, ruleChainAssociates);
            // TbActiveStatusNodeConfiguration
            objectNode.put("deviceIds", activeStatusNodeData.getDeviceIds());
            if (isShowOnWidget) {
                objectNode.put("showOnWidget", true);
            }

            ruleNode.setType("org.thingsboard.rule.engine.customize.activestatus.TbActiveStatusNode");
        } else if (RuleTriggerEnum.LOW_BATTERY.eq(trigger)) {
            LowBatteryNodeData lowBatteryNodeData = convertValue("jsonData", jsonData, LowBatteryNodeData.class);
            checkParameter("threshold", String.valueOf(lowBatteryNodeData.getThreshold()));

            String deviceIds = lowBatteryNodeData.getDeviceIds();
            // 过滤条件关系
            String type = RuleChainAssociateTypeEnum.DEVICE_NODE.getValue();
            addTriggerAssociate(deviceIds, ruleNodeId, type, tenantId, ruleChainAssociates);
            // TbLowBatteryNodeConfiguration
            objectNode.put("threshold", lowBatteryNodeData.getThreshold());
            objectNode.put("deviceIds", lowBatteryNodeData.getDeviceIds());
            if (isShowOnWidget) {
                objectNode.put("showOnWidget", true);
            }

            ruleNode.setType("org.thingsboard.rule.engine.customize.lowbattery.TbLowBatteryNode");
        }
        // 显示到仪表板需要使用
        objectNode.put("ruleChainOwnId", entityData.getRuleChainOwnId().getId().toString());
        objectNode.put("ruleChainName", entityData.getName());
        // 设置节点setConfiguration
        ruleNode.setConfiguration(objectNode);
        // rule_node_associate 关联设备,通道
        ruleChainAssociateService.saveAll(ruleChainAssociates);

        // 保存主节点
        baseRuleChainService.saveRuleNode(tenantId, ruleNode);
    }

    private static void verifyRecipients(RuleChainOwn entityData, String recipients, List<Recipients> recipientsList,
                                         List<String> effectiveRecipientsIdList) throws ThingsboardException {
        recipientsList.forEach(item -> {
            String recipientsId = item.getRecipientsId().getId().toString();
            if (recipients.contains(recipientsId)) {
                effectiveRecipientsIdList.add(recipientsId);
            }
        });
        // 失败
        if (CollectionUtils.isEmpty(effectiveRecipientsIdList)) {
            log.error("Parameter recipients invalid failure, recipients = {} ,name = {} ", recipients, entityData.getName());
            throw new ThingsboardException("Parameter failed,Parameter recipients invalid failure",
                    ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
    }


    private void addEntityRelation(List<EntityRelation> relations, RuleNode mainRuleNode, RuleTriggerEnum ruleTriggerEnum,
                                   RuleChain ruleChain) {
        // 需要增加两条关系
        String relationType = ruleTriggerEnum.getRelationType();
        RuleNodeId nodeId = mainRuleNode.getId();
        EntityRelation ruleNodeRelation
                = getMainEntityRelation(ruleChain.getFirstRuleNodeId(), nodeId, relationType, RelationTypeGroup.RULE_NODE);
        relations.add(ruleNodeRelation);
        // 包含关系 Contains
        EntityRelation ruleChainRelation
                = getMainEntityRelation(ruleChain.getId(), nodeId, EntityRelation.CONTAINS_TYPE, RelationTypeGroup.RULE_CHAIN);
        relations.add(ruleChainRelation);
    }

    @NotNull
    private static EntityRelation getMainEntityRelation(EntityId fromEntityId, EntityId toEntityId, String relationType,
                                                        RelationTypeGroup relationTypeGroup) {
        EntityRelation entityRelation = new EntityRelation();
        entityRelation.setFrom(fromEntityId);
        entityRelation.setTo(toEntityId);
        entityRelation.setType(relationType);
        entityRelation.setTypeGroup(relationTypeGroup);
        return entityRelation;
    }

    @NotNull
    private static ObjectNode getAdditionalInfoJsonNodes(int layoutX, int bound) {
        ObjectNode objectNode = JacksonUtil.newObjectNode();
        objectNode.put("description", "");
        objectNode.put("layoutX", layoutX);
        objectNode.put("layoutY", new Random().nextInt(bound));
        return objectNode;
    }


    /**
     * 添加关联关系
     *
     * @param associates          关系列表
     * @param ruleNodeId          规则节点ID
     * @param type                关系类型
     * @param tenantId            租户ID
     * @param ruleChainAssociates 关系数组
     */
    private void addTriggerAssociate(String associates, UUID ruleNodeId, String type, TenantId tenantId,
                                     List<RuleChainAssociate> ruleChainAssociates) {
        if (StringUtils.isBlank(associates)) {
            log.info("No associated data ruleNodeId = {}", ruleNodeId);
            return;
        }
        List<RuleChainAssociate> collect = Arrays.stream(associates.split(StringUtils.COMMA))
                .map(associateId -> getRuleNodeAssociate(ruleNodeId, type, tenantId, UUID.fromString(associateId)))
                .collect(Collectors.toList());
        ruleChainAssociates.addAll(collect);
    }

    private RuleChainAssociate getRuleNodeAssociate(UUID ruleId, String tableName, TenantId tenantId, UUID associateId) {
        return RuleChainAssociate.builder()
                .associateId(associateId)
                .type(tableName)
                .ruleId(ruleId)
                .tenantId(tenantId.getId())
                .build();
    }


    @Override
    @Transactional
    public RuleChainOwn deleteRuleChainOwn(TenantId tenantId, UUID primaryId) throws ThingsboardException {
        // 校验是否存在
        RuleChainOwn ruleChainOwn = findByIdOrThrow(tenantId, primaryId);
        // 删除
        baseDao.removeById(tenantId, primaryId);

        // 删除 接收方 - (二开) 规则节点 直接关系
        UUID id = ruleChainOwn.getRuleChainOwnId().getId();
        ruleChainAssociateService.deleteByRuleId(id);
        // （二开）规则链和（原）规则链关系
        ruleChainAssociateService.deleteByAssociateId(id);
        String trigger = ruleChainOwn.getTrigger();
        RuleTriggerEnum ruleTriggerEnum = IEnum.of(RuleTriggerEnum.class, trigger);
        Boolean needAddNode = ruleTriggerEnum.getNeedAddNode();

        // 删除这个关系节点的所有关系
        UUID ruleNodeId = ruleChainOwn.getRuleNodeId();
        if (needAddNode && Objects.nonNull(ruleNodeId)) {
            // 删除这个节点的关系
            ruleChainAssociateService.deleteByRuleId(ruleNodeId);
            // 删除单个节点
            baseRuleChainService.deleteRuleNode(tenantId, new RuleNodeId(ruleNodeId));
        }

        log.info("remove RuleChainOwn name = {} , id = {} ", ruleChainOwn.getName(), ruleChainOwn.getRuleChainOwnId());

        return ruleChainOwn;
    }

    @Override
    public RuleChainOwn getRuleChainOwnById(TenantId tenantId, UUID primaryId) throws ThingsboardException {
        return findByIdOrThrow(tenantId, primaryId);
    }

    @Override
    public RuleChainOwn getByTenantIdAndName(TenantId tenantId, String name) {
        return baseDao.getByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    @Transactional
    public List<RuleChainId> deleteRecipients(TenantId tenantId, Recipients recipients) {
        RecipientsId recipientsId = recipients.getRecipientsId();
        List<RuleChainAssociate> list = ruleChainAssociateService.findByRecipientsId(recipientsId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<RuleChainId> ruleChainIds = new ArrayList<>();
        list.forEach(ruleChainAssociate -> {
            String type = ruleChainAssociate.getType();
            UUID ruleId = ruleChainAssociate.getRuleId();
            if (RuleChainAssociateTypeEnum.RECIPIENTS_RULE_CHAIN_OWN.eq(type)) {
                // do nothing
                log.trace("{} do nothing !", type);
            } else if (!RuleChainAssociateTypeEnum.RECIPIENTS_CHAIN.eq(type)) {
                // 删除单个节点
                baseRuleChainService.deleteRuleNode(tenantId, new RuleNodeId(ruleId));
            } else {
                ruleChainIds.add(new RuleChainId(ruleId));
            }
        });
        // 删除其他节点关系
        ruleChainAssociateService.deleteByAssociateId(recipientsId.getId());
        return ruleChainIds;
    }

    @Override
    public List<RuleChainOwn> findRuleChainOwnByRecipientId(TenantId tenantId, String recipientsId) {
        RecipientsId id = new RecipientsId(UUID.fromString(recipientsId));
        return baseDao.findRuleChainOwnByRecipientId(id);
    }

}
