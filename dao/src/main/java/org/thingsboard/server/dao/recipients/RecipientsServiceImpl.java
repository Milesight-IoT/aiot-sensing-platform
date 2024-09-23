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

package org.thingsboard.server.dao.recipients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.recipients.RecipientsDto;
import org.thingsboard.server.common.data.recipients.jsondata.HttpTransportJsonData;
import org.thingsboard.server.common.data.recipients.jsondata.MqttTransportJsonData;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNode;
import org.thingsboard.server.common.enume.recipients.TransmissionTypeEnum;
import org.thingsboard.server.common.enume.rule.RuleChainAssociateTypeEnum;
import org.thingsboard.server.dao.BaseDaoServiceImpl;
import org.thingsboard.server.dao.rule.BaseRuleChainService;
import org.thingsboard.server.dao.rule.RuleChainAssociateService;
import org.thingsboard.server.dao.rule.RuleChainOwnService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 接收方网络配置管理 - 服务实现
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:08
 */
@Service
@Slf4j
public class RecipientsServiceImpl extends BaseDaoServiceImpl<RecipientsDao, Recipients> implements RecipientsService {
    @Resource
    private RuleChainOwnService ruleChainOwnService;
    @Resource
    private RuleChainAssociateService ruleChainAssociateService;
    @Resource
    private BaseRuleChainService ruleChainService;

    @Override
    public PageData<Recipients> findRecipientsList(TenantId tenantId, PageLink pageLink) {
        return baseDao.findRecipientsList(tenantId.getId(), pageLink);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Recipients createOrUpdate(TenantId tenantId, RecipientsDto dto) throws ThingsboardException {
        // 检查属性
        checkRecipientsDto(tenantId, dto);
        // createOrUpdate
        Recipients recipients = Objects.nonNull(dto.getRecipientsId())
                ? findByIdOrThrow(tenantId, dto.getRecipientsId().getId()) : new Recipients();
        // 复制属性
        BeanUtils.copyProperties(dto, recipients, Recipients.class);

        recipients.setUpdateTime(System.currentTimeMillis());
        recipients.setTenantId(tenantId.getId());

        // 添加规则链节点
        updateRuleChainNodes(tenantId, recipients);

        Recipients save = baseDao.save(tenantId, recipients);
        save.setRuleChainIds(recipients.getRuleChainIds());

        log.info("createOrUpdate Recipients name = {} , id = {}", dto.getName(), save.getRecipientsId());
        return save;
    }

    private void updateRuleChainNodes(TenantId tenantId, Recipients recipients) {
        RecipientsId recipientsId = recipients.getRecipientsId();
        if (Objects.isNull(recipientsId)) {
            // 新增无ID不需要更改规则链
            return;
        }

        List<RuleChainAssociate> list = ruleChainAssociateService.findByRecipientsId(recipientsId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<RuleChainId> ruleChainIds = new ArrayList<>();

        list.forEach(ruleChainAssociate -> {
            String type = ruleChainAssociate.getType();
            if (RuleChainAssociateTypeEnum.RECIPIENTS_CHAIN.eq(type) || RuleChainAssociateTypeEnum.RECIPIENTS_RULE_CHAIN_OWN.eq(type)) {
                // 不需要处理 接收方和规则链关系（只用预判是否存在该规则链）
                return;
            }
            UUID ruleNodeId = ruleChainAssociate.getRuleId();
            RuleNode recipientsNode = ruleChainService.findRuleNodeById(tenantId, new RuleNodeId(ruleNodeId));
            if (recipientsNode == null) {
                log.debug("Rule node with id does not exist ruleNodeId= {}", ruleNodeId);
                return;
            }
            // 重新设置属性
            recipientsNode.setName(recipients.getName());
            setConfigurationAndType(recipients, recipientsNode);
            // 单个节点
            ruleChainService.saveRuleNode(tenantId, recipientsNode);
            // 刷新规则链
            ruleChainIds.add(recipientsNode.getRuleChainId());
        });
        recipients.setRuleChainIds(ruleChainIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RuleChainId> deleteRecipients(TenantId tenantId, UUID recipientsId) throws ThingsboardException {
        // 校验是否存在
        Recipients recipients = findByIdOrThrow(tenantId, recipientsId);
        // 删除
        baseDao.removeById(tenantId, recipientsId);
        //  删除后更改规则节点配置
        log.info("deleteRecipients name = {}, id = {} ,", recipients.getName(), recipientsId);

        return ruleChainOwnService.deleteRecipients(tenantId, recipients);
    }

    @Override
    public Recipients getRecipientsById(TenantId tenantId, UUID recipientsId) throws ThingsboardException {
        return findByIdOrThrow(tenantId, recipientsId);
    }

    @Override
    public List<Recipients> findRecipientsByIds(String recipientsIds) {
        List<UUID> collect = Arrays.stream(recipientsIds.split(StringUtils.COMMA))
                .filter(StringUtils::isNotBlank).map(UUID::fromString).collect(Collectors.toList());
        return baseDao.findByIds(collect);
    }

    @Override
    public List<Recipients> findByIds(List<UUID> uuidList) {
        if (CollectionUtils.isEmpty(uuidList)) {
            return Collections.emptyList();
        }
        return baseDao.findByIds(uuidList);
    }

    @Override
    public Recipients getByTenantIdAndName(TenantId tenantId, String name) {
        return baseDao.getByTenantIdAndName(tenantId.getId(), name);
    }

    /**
     * 检查参数
     *
     * @param tenantId 租户ID
     * @param dto      入参
     * @throws ThingsboardException 业务异常
     */
    private void checkRecipientsDto(TenantId tenantId, RecipientsDto dto) throws ThingsboardException {
        // 校验重名
        Recipients recipients = getByTenantIdAndName(tenantId, dto.getName());
        if (recipients != null && !recipients.getRecipientsId().equals(dto.getRecipientsId())) {
            log.error("Recipients is exist! id ={} , RecipientsName = {}", dto.getRecipientsId(), dto.getName());
            throw new ThingsboardException("Recipients already exists!", ThingsboardErrorCode.GENERAL);
        }

        String transmissionType = dto.getTransmissionType();
        Object jsonData = dto.getJsonData();
        if (TransmissionTypeEnum.HTTP.eq(transmissionType)) {
            HttpTransportJsonData data = convertValue("jsonData", jsonData, HttpTransportJsonData.class);
            checkParameter("jsonData url ", data.getUrl());
        } else if (TransmissionTypeEnum.MQTT.eq(transmissionType)) {
            MqttTransportJsonData data = convertValue("jsonData", jsonData, MqttTransportJsonData.class);
            checkParameter("jsonData host ", data.getHost());
            checkParameter("jsonData port ", String.valueOf(data.getPort()));
            checkParameter("jsonData topic ", data.getTopic());
        }
    }

    @Override
    public void setConfigurationAndType(Recipients recipients, RuleNode recipientsNode) {
        ObjectNode configuration = JacksonUtil.OBJECT_MAPPER.createObjectNode();
        String transmissionType = recipients.getTransmissionType();
        Object jsonData = recipients.getJsonData();
        if (TransmissionTypeEnum.MQTT.eq(transmissionType)) {
            MqttTransportJsonData mqttTransportJsonData = JacksonUtil.OBJECT_MAPPER.convertValue(jsonData, MqttTransportJsonData.class);
            configuration.put("topicPattern", mqttTransportJsonData.getTopic());
            configuration.put("host", mqttTransportJsonData.getHost());
            configuration.put("port", mqttTransportJsonData.getPort());
            configuration.put("connectTimeoutSec", 10);
            configuration.put("cleanSession", true);
            configuration.put("retainedMessage", false);
            configuration.put("ssl", false);

            recipientsNode.setType("org.thingsboard.rule.engine.mqtt.TbMqttNode");
        } else {
            HttpTransportJsonData httpTransportJsonData = JacksonUtil.OBJECT_MAPPER.convertValue(jsonData, HttpTransportJsonData.class);
            configuration.put("restEndpointUrlPattern", httpTransportJsonData.getUrl());
            configuration.put("requestMethod", "POST");
            configuration.set("headers", JacksonUtil.OBJECT_MAPPER.createObjectNode().put("Content-Type", "application/json"));
            configuration.put("useSimpleClientHttpFactory", false);
            configuration.put("readTimeoutMs", 10000);
            configuration.put("maxParallelRequestsCount", 0);
            configuration.put("useRedisQueueForMsgPersistence", false);
            configuration.put("trimQueue", false);
            configuration.put("maxQueueSize", 0);
            configuration.put("enableProxy", false);
            configuration.put("useSystemProxyProperties", false);
            configuration.put("proxyPort", 0);
            configuration.put("ignoreRequestBody", false);

            recipientsNode.setType("org.thingsboard.rule.engine.rest.TbRestApiCallNode");
        }
        // 校验属性
        ObjectNode credentials = JacksonUtil.OBJECT_MAPPER.createObjectNode();
        if (StringUtils.isEmpty(recipients.getUsername())) {
            credentials.put("type", "anonymous");
        } else {
            credentials.put("type", "basic");
            credentials.put("username", recipients.getUsername());
            credentials.put("password", recipients.getPassword());
        }
        configuration.set("credentials", credentials);
        recipientsNode.setConfiguration(configuration);
    }
}
