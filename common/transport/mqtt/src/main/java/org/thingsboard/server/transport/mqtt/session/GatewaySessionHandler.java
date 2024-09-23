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
package org.thingsboard.server.transport.mqtt.session;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.thingsboard.server.common.transport.adaptor.AdaptorException;
import org.thingsboard.server.common.transport.auth.GetOrCreateDeviceFromGatewayResponse;

import java.util.UUID;

/**
 * Created by nickAS21 on 26.12.22
 */
public class GatewaySessionHandler extends AbstractGatewaySessionHandler {

    public GatewaySessionHandler(DeviceSessionCtx deviceSessionCtx, UUID sessionId) {
        super(deviceSessionCtx, sessionId);
    }

    public void onDeviceConnect(MqttPublishMessage mqttMsg) throws AdaptorException {
        if (isJsonPayloadType()) {
            onDeviceConnectJson(mqttMsg);
        } else {
            onDeviceConnectProto(mqttMsg);
        }
    }

    public void onDeviceTelemetry(MqttPublishMessage mqttMsg) throws AdaptorException {
        int msgId = getMsgId(mqttMsg);
        ByteBuf payload = mqttMsg.payload();
        if (isJsonPayloadType()) {
            onDeviceTelemetryJson(msgId, payload);
        } else {
            onDeviceTelemetryProto(msgId, payload);
        }
    }

    @Override
    protected GatewayDeviceSessionContext newDeviceSessionCtx(GetOrCreateDeviceFromGatewayResponse msg) {
         return new GatewayDeviceSessionContext(this, msg.getDeviceInfo(), msg.getDeviceProfile(), mqttQoSMap, transportService);
    }

}
