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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApiModel
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRecognition {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(position = 1, value = "设备能力ID")
    private UUID id;

    @ApiModelProperty(position = 2, value = "创建时间")
    private long createdTime;

    @ApiModelProperty(position = 3, value = "更新时间")
    private long updatedTime;

    @ApiModelProperty(position = 4, value = "设备ID")
    private UUID deviceId;

    @ApiModelProperty(position = 5, value = "上传遥测数据时间")
    private long ts;

    @ApiModelProperty(position = 6, value = "设备能力")
    private String ability;

    @ApiModelProperty(position = 7, value = "能力类型  1-基础能力; 2-图片能力")
    private short abilityType;

    @ApiModelProperty(position = 8, value = "能力类型为1时, 值为基础遥测数据, 格式为string; 能力类型为2时, 值为识别属性值, jsonstring格式")
    private String value;

    @ApiModelProperty(position = 9, value = "额外信息  当ability_type = 1时, 值为空; 当ability_type = 2时, 值代表坐标 \"11,12,13,14\"")
    private String extraInfo;
}
