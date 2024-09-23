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

package org.thingsboard.server.common.enume.recipients;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thingsboard.server.common.enume.IEnum;

/**
 * 网络传输类型
 *
 * @author Luohh
 * @version 1.0.0
 * @date 2023年4月3日16:34:11
 */
@Getter
@AllArgsConstructor
public enum TransmissionTypeEnum implements IEnum<String> {
    /**
     * 网络传输类型
     */
    HTTP("HTTP Post", "HTTP"),
    MQTT("MQTT", "MQTT");

    private final String value;
    private final String label;
}
