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

@ApiModel
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentDeviceOtaPackage {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("固件标题")
    private String fwTitle;

    @ApiModelProperty("固件校验和")
    private String fwChecksum;

    @ApiModelProperty("配置文件标题")
    private String cfTitle;

    @ApiModelProperty("配置文件校验和")
    private String cfChecksum;
}
