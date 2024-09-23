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

package org.thingsboard.server.common.data.rule;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.UUID;

/**
 * 各模块租户规则链信息关联表 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:39
 */
@ApiModel
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RuleChainAssociate implements Serializable {

    /**
     * 各模块租户规则链信息关联表
     */
    @ApiModelProperty(position = 1, value = "初始化租户规则链信息关联")
    private UUID id;

    /**
     * 创建时间
     */
    @ApiModelProperty(position = 2, value = "创建时间")
    private long createdTime;

    /**
     * 租户ID
     */
    @ApiModelProperty(position = 3, value = "租户ID")
    private UUID tenantId;

    /**
     * type
     */
    private String type;

    /**
     * 关联主键ID
     */
    @ApiModelProperty(position = 4, value = "关联主键ID,根据type字段变换表")
    private UUID associateId;

    /**
     * 规则链节点/规则链ID
     */
    @ApiModelProperty(position = 5, value = "规则链节点/规则链ID/")
    private UUID ruleId;
}

