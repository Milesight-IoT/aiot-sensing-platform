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
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.mqtt.MqttClient;
import org.thingsboard.mqtt.MqttClientConfig;
import org.thingsboard.mqtt.MqttConnectResult;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.rule.engine.mqtt.TbMqttNodeConfiguration;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.recipients.jsondata.MqttTransportJsonData;
import org.thingsboard.server.common.msg.TbMsg;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MQTT 发送ROI结果给第三方
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.EXTERNAL,
        name = "roi mqtt",
        relationTypes = {"True", "False"},
        configClazz = TbMqttNodeConfiguration.class,
        nodeDescription = "当收到推理平台传的识别结果时会触发该条规则(MQTT)",
        nodeDetails = "当收到推理平台传的识别结果时会触发该条规则(MQTT)"
)
public class RoiMqttNode implements TbNode {

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws Exception {
        TbMqttNodeConfiguration mqttNodeConfiguration = new TbMqttNodeConfiguration().defaultConfiguration();
        MqttClient mqttClient;
        try {
            mqttClient = initClient(ctx, msg, mqttNodeConfiguration);
        } catch (Exception e) {
            String hostPort = mqttNodeConfiguration.getHost() + ":" + mqttNodeConfiguration.getPort();
            log.error(hostPort + "failed sending message: ", e.getMessage());
            ctx.tellFailure(msg, new TbNodeException(e.getMessage()));
            return;
        }
        // 发送MQTT请求
        String topic = TbNodeUtils.processPattern(mqttNodeConfiguration.getTopicPattern(), msg);
        mqttClient.publish(topic, Unpooled.wrappedBuffer(msg.getData().getBytes(StandardCharsets.UTF_8)),
                        MqttQoS.AT_LEAST_ONCE, mqttNodeConfiguration.isRetainedMessage())
                .addListener(future ->
                        {
                            if (future.isSuccess()) {
                                ctx.tellSuccess(msg);
                            } else {
                                String hostPort = mqttNodeConfiguration.getHost() + ":" + mqttNodeConfiguration.getPort();
                                log.error(hostPort + " failed sending message back!");
                                ctx.tellFailure(msg, future.cause());
                            }
                            mqttClient.disconnect();
                        }
                );
    }

    private MqttClient initClient(TbContext ctx, TbMsg msg, TbMqttNodeConfiguration mqttNodeConfiguration) throws JsonProcessingException {
        String recipientsInfo = msg.getMetaData().getValue("recipientsInfo");
        Recipients recipients = JacksonUtil.OBJECT_MAPPER.readValue(recipientsInfo, Recipients.class);
        MqttTransportJsonData transportJsonData = JacksonUtil.OBJECT_MAPPER.convertValue(recipients.getJsonData(), MqttTransportJsonData.class);

        mqttNodeConfiguration.setHost(transportJsonData.getHost());
        mqttNodeConfiguration.setPort(transportJsonData.getPort());
        mqttNodeConfiguration.setTopicPattern(transportJsonData.getTopic());

        MqttClientConfig config = new MqttClientConfig(null);
        if (!StringUtils.isEmpty(mqttNodeConfiguration.getClientId())) {
            config.setClientId(mqttNodeConfiguration.isAppendClientIdSuffix() ?
                    mqttNodeConfiguration.getClientId() + "_" + ctx.getServiceId() : mqttNodeConfiguration.getClientId());
        }
        config.setCleanSession(false);

        prepareMqttClientConfig(config, recipients);
        MqttClient client = MqttClient.create(config, null);
        client.setEventLoop(ctx.getSharedEventLoop());

        log.info("init tb mqtt client node configuration transportJsonData = {},username = {},password={}", recipients.getJsonData(),
                recipients.getUsername(), recipients.getPassword());

        Future<MqttConnectResult> connectFuture = client.connect(mqttNodeConfiguration.getHost(), mqttNodeConfiguration.getPort());
        MqttConnectResult result;
        try {
            result = connectFuture.get(mqttNodeConfiguration.getConnectTimeoutSec(), TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            connectFuture.cancel(true);
            client.disconnect();
            String hostPort = mqttNodeConfiguration.getHost() + ":" + mqttNodeConfiguration.getPort();
            String format = String.format("Failed to connect to MQTT broker at %s.", hostPort);
            log.error(format);
            throw new RuntimeException(format);
        }

        if (!result.isSuccess()) {
            connectFuture.cancel(true);
            client.disconnect();
            String hostPort = mqttNodeConfiguration.getHost() + ":" + mqttNodeConfiguration.getPort();
            String format = String.format("Failed to connect to MQTT broker at %s. Result code is: %s", hostPort, result.getReturnCode());
            log.error(format);
            throw new RuntimeException(format);
        }
        return client;
    }

    private void prepareMqttClientConfig(MqttClientConfig config, Recipients recipients) {
        String username = recipients.getUsername();
        if (StringUtils.isNotBlank(username)) {
            config.setUsername(username);
            config.setPassword(recipients.getPassword());
        }
    }

}
