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

package org.thingsboard.server.common.data.recognition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 遥测数据传递第三方信息 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/14 13:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryExtData implements Serializable {
    /**
     * 设备数据
     */
    private Object deviceInfo;
    /**
     * 感知数据
     */
    private List<TelemetryRecognitionThird> telemetryRecognitionThirds;
}