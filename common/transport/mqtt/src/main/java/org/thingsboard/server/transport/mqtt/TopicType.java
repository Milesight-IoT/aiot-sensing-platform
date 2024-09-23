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
package org.thingsboard.server.transport.mqtt;

import lombok.Getter;
import org.thingsboard.server.common.data.device.profile.MqttTopics;

public enum TopicType {

    V1(MqttTopics.BASIC_DEVICES_MS_TOPIC, MqttTopics.DEVICE_ATTRIBUTES_RESPONSE_TOPIC_PREFIX, MqttTopics.DEVICE_ATTRIBUTES_TOPIC, MqttTopics.DEVICE_RPC_REQUESTS_TOPIC, MqttTopics.DEVICE_RPC_RESPONSE_TOPIC),
    V2(MqttTopics.BASIC_DEVICES_MS_TOPIC, MqttTopics.DEVICE_ATTRIBUTES_RESPONSE_SHORT_TOPIC_PREFIX, MqttTopics.DEVICE_ATTRIBUTES_SHORT_TOPIC, MqttTopics.DEVICE_RPC_REQUESTS_SHORT_TOPIC, MqttTopics.DEVICE_RPC_RESPONSE_SHORT_TOPIC),
    V2_JSON(MqttTopics.BASIC_DEVICES_MS_TOPIC, MqttTopics.DEVICE_ATTRIBUTES_RESPONSE_SHORT_JSON_TOPIC_PREFIX, MqttTopics.DEVICE_ATTRIBUTES_SHORT_JSON_TOPIC, MqttTopics.DEVICE_RPC_REQUESTS_SHORT_JSON_TOPIC, MqttTopics.DEVICE_RPC_RESPONSE_SHORT_JSON_TOPIC),
    V2_PROTO(MqttTopics.BASIC_DEVICES_MS_TOPIC, MqttTopics.DEVICE_ATTRIBUTES_RESPONSE_SHORT_PROTO_TOPIC_PREFIX, MqttTopics.DEVICE_ATTRIBUTES_SHORT_PROTO_TOPIC, MqttTopics.DEVICE_RPC_REQUESTS_SHORT_PROTO_TOPIC, MqttTopics.DEVICE_RPC_RESPONSE_SHORT_PROTO_TOPIC);

    @Getter
    private final String msTopicBase;

    @Getter
    private final String attributesResponseTopicBase;

    @Getter
    private final String attributesSubTopic;

    @Getter
    private final String rpcRequestTopicBase;

    @Getter
    private final String rpcResponseTopicBase;

    TopicType(String msTopicBase, String attributesRequestTopicBase, String attributesSubTopic, String rpcRequestTopicBase, String rpcResponseTopicBase) {
        this.msTopicBase = msTopicBase;
        this.attributesResponseTopicBase = attributesRequestTopicBase;
        this.attributesSubTopic = attributesSubTopic;
        this.rpcRequestTopicBase = rpcRequestTopicBase;
        this.rpcResponseTopicBase = rpcResponseTopicBase;
    }
}
