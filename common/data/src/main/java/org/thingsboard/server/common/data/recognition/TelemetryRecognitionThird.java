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
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 提供给第三方使用对象
 *
 * @author Luohh
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRecognitionThird implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据库主键ID
     */
    @NotBlank
    private String id;
    /**
     * 遥测属性名称
     */
    @NotBlank
    private String ability;
    /**
     * 能力类型  1-基础能力; 2-图片能力
     */
    @NotBlank
    private short abilityType;
    /**
     * 能力类型为1时, 值为基础遥测数据, 格式为string; 能力类型为2时, 值为识别属性值, jsonString格式
     */
    private String value;
    /**
     * 额外信息  当ability_type = 1时, 值为空; 当ability_type = 2时, 值代表坐标 "11,12,13,14"
     */
    private String extraInfo;

    public TelemetryRecognitionThird(TelemetryRecognition telemetryRecognition) {
        this.id = telemetryRecognition.getId().toString();
        this.ability = telemetryRecognition.getAbility();
        this.abilityType = telemetryRecognition.getAbilityType();
        this.value = telemetryRecognition.getValue();
        this.extraInfo = telemetryRecognition.getExtraInfo();
    }
}
