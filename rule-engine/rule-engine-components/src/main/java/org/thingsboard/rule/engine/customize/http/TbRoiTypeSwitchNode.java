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
package org.thingsboard.rule.engine.customize.http;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.EmptyNodeConfiguration;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.UUID;

/**
 * 查询ROI推送对象（Http，Mqtt）
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "roi message type switch",
        configClazz = EmptyNodeConfiguration.class,
        relationTypes = {"HTTP Post", "MQTT"},
        nodeDescription = "查询ROI推送对象",
        nodeDetails = "查询ROI推送对象"
)
public class TbRoiTypeSwitchNode implements TbNode {

    EmptyNodeConfiguration config;

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, EmptyNodeConfiguration.class);
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) {
        String recipientsId = msg.getMetaData().getValue("recipientsId");
        if (recipientsId == null) {
            log.error("Recipients is not find id = {}", recipientsId);
            ctx.tellFailure(msg, new TbNodeException("No such recipientsId found."));
            return;
        }
        Recipients recipientsById;
        try {
            recipientsById = ctx.getRecipientsService().getRecipientsById(ctx.getTenantId(), UUID.fromString(recipientsId));
        } catch (ThingsboardException e) {
            log.error("Recipients is not find id = {}", recipientsId);
            ctx.tellFailure(msg, new TbNodeException("No such recipientsId found."));
            return;
        }
        String transmissionType = recipientsById.getTransmissionType();
        msg.getMetaData().putValue("recipientsInfo", JacksonUtil.toString(recipientsById));
        ctx.tellNext(msg, transmissionType);
    }

}
