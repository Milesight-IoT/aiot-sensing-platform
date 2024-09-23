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
package org.thingsboard.server.common.data;

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
public class AttributeRecognition {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(position = 1, value = "属性识别ID")
    private UUID id;

    @ApiModelProperty(position = 2, value = "创建时间")
    private long createdTime;

    @ApiModelProperty(position = 3, value = "租户ID")
    private UUID tenantId;

    @ApiModelProperty(position = 4, value = "属性类型,默认0\u000B0 - 自定义属性（当前租户可见）\u000B1 - 系统属性（所有租户共享）")
    private short attributeType;

    @ApiModelProperty(position = 5, value = "当属性类型为系统属性时,\"1\"-\"License Plate\";  \"2\"-\"Plate Color\";  \"3\"-\"Plate Type\";  \"4\"-\"Vehicle Type\"\u000B当属性类型为自定义属性时,值是用户填写的")
    private String attribute;
}
