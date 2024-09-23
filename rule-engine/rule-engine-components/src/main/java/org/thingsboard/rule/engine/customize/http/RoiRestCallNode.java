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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.credentials.BasicCredentials;
import org.thingsboard.rule.engine.rest.TbHttpClient;
import org.thingsboard.rule.engine.rest.TbRestApiCallNodeConfiguration;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.recipients.jsondata.HttpTransportJsonData;
import org.thingsboard.server.common.msg.TbMsg;

/**
 * HTTP 发送ROI结果给第三方
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.EXTERNAL,
        name = "roi rest api call",
        relationTypes = {"True", "False"},
        configClazz = TbRestApiCallNodeConfiguration.class,
        nodeDescription = "当收到推理平台传的识别结果时会触发该条规则(HTTP)",
        nodeDetails = "当收到推理平台传的识别结果时会触发该条规则(HTTP)"
)
public class RoiRestCallNode implements TbNode {

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {

    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws TbNodeException {
        TbRestApiCallNodeConfiguration config = new TbRestApiCallNodeConfiguration().defaultConfiguration();
        try {
            initTbRestApiCallNodeConfiguration(config, msg);
        } catch (JsonProcessingException e) {
            log.error("init tb rest api call node configuration url = {} failed:", config.getRestEndpointUrlPattern(), e);
            ctx.tellFailure(msg, new TbNodeException("JsonProcessingException recipients."));
            return;
        }
        TbHttpClient httpClient = new TbHttpClient(config, ctx.getSharedEventLoop());
        // 发送HTTP请求
        httpClient.processMessage(ctx, msg);
        httpClient.destroy();
    }

    private void initTbRestApiCallNodeConfiguration(TbRestApiCallNodeConfiguration config, TbMsg msg) throws JsonProcessingException {
        String recipientsInfo = msg.getMetaData().getValue("recipientsInfo");
        Recipients recipients = JacksonUtil.OBJECT_MAPPER.readValue(recipientsInfo, Recipients.class);
        HttpTransportJsonData transportJsonData = JacksonUtil.OBJECT_MAPPER.convertValue(recipients.getJsonData(), HttpTransportJsonData.class);

        String url = transportJsonData.getUrl();
        config.setRestEndpointUrlPattern(url);
        String username = recipients.getUsername();
        String password = recipients.getPassword();
        log.info("init tb rest api call node configuration url = {},username = {},password={}", url, username, password);
        if (StringUtils.isNotBlank(username)) {
            BasicCredentials basicCredentials = new BasicCredentials();
            basicCredentials.setUsername(username);
            basicCredentials.setPassword(password);
            config.setCredentials(basicCredentials);
        }
    }
}
