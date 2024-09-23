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

package org.thingsboard.rule.engine.customize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.recognition.TelemetryExtData;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.HashMap;
import java.util.Map;

/**
 * 推送数据处理
 *
 * @author Luohh
 */
public class PerDataProcessUtil {

    public static ObjectNode process(TbMsg msg, EntityId originator) {
        ObjectNode objectNode = JacksonUtil.newObjectNode();
        JsonNode jsonNode = JacksonUtil.toJsonNode(msg.getData());
        TelemetryExtData telemetryExtData = JacksonUtil.FAIL_ON_UNKNOWN_PROPERTIES_MAPPER.convertValue(jsonNode.get("telemetryExtData"), TelemetryExtData.class);
        if (telemetryExtData == null) {
            Map<String, Object> dataMap = new HashMap<>(msg.getMetaData().getData());
            dataMap.put("deviceId", originator.getId().toString());
            telemetryExtData = TelemetryExtData.builder().deviceInfo(dataMap).build();
        }
        telemetryExtData.setTelemetryRecognitionThirds(null);

        objectNode.set("telemetryExtData", JacksonUtil.valueToTree(telemetryExtData));
        return objectNode;
    }

    public static ObjectNode processActiveMsg(TbMsg msg, EntityId originator) {
        ObjectNode objectNode = JacksonUtil.newObjectNode();

        Map<String, Object> dataMap = new HashMap<>(msg.getMetaData().getData());
        dataMap.put("deviceId", originator.getId().toString());
        TelemetryExtData telemetryExtData = TelemetryExtData.builder().deviceInfo(dataMap).build();

        objectNode.set("telemetryExtData", JacksonUtil.valueToTree(telemetryExtData));
        return objectNode;
    }
}
