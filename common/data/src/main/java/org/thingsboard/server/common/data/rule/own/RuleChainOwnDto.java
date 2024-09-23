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

package org.thingsboard.server.common.data.rule.own;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.data.id.RuleChainOwnId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 规则链表（二开） - 实体入参
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:39
 */
@Slf4j
@Data
public class RuleChainOwnDto implements Serializable {
    /**
     * 主键ID
     * EntityType.RULE_CHAIN_OWN
     */
    private RuleChainOwnId ruleChainOwnId;

    /**
     * 规则链名称
     */
    @NotBlank
    private String name;

    /**
     * 触发器
     */
    @NotBlank
    private String trigger;

    /**
     * 动作（多个动作逗号隔开）
     */
    @NotBlank
    private String actions;

    /**
     * 不同触发器不同的对象,json
     * Low battery：
     * {
     *     "threshold":10,// 电量
     *     "deviceIds":"123,456",// 设备ID,多个逗号隔开
     *     "recipients":"12,123" // 接收方,多个逗号隔开
     * }
     * Devices become inactive：
     * {
     *     "deviceIds":"123,456",//设备ID 多个逗号隔开
     *     "recipients":"12,123" // 接收方,多个逗号隔开
     * }
     * Once data received：
     * {
     *     "sensingObjectIds":"123,456",//感知通道ID 多个逗号隔开
     *     "recipients":"12,123" // 接收方,多个逗号隔开
     * }
     */
    @NotNull(message = "jsonData Can not be empty!")
    private Object jsonData;
}

