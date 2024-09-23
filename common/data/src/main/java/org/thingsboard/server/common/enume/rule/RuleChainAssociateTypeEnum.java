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

package org.thingsboard.server.common.enume.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thingsboard.server.common.enume.IEnum;

/**
 * rule_chain_associate 类型-枚举 A-R
 *
 * @author Luohh
 * @version 1.0.0
 * @date 2023年4月3日16:34:11
 */
@Getter
@AllArgsConstructor
public enum RuleChainAssociateTypeEnum implements IEnum<String> {
    /**
     * 接收人-感知对象的通道规则节点
     */
    RECIPIENTS_RECEIVED_NODE("RECIPIENTS_RECEIVED_NODE", "Once data received"),
    /**
     * 接收人-电量规则节点
     */
    RECIPIENTS_BATTERY_NODE("RECIPIENTS_BATTERY_NODE", "Low battery"),
    /**
     * 接收人-活跃状态
     */
    RECIPIENTS_ACTIVE_NODE("RECIPIENTS_ACTIVE_NODE", "Devices become inactive"),
    /**
     * 接收人 - （原）规则节点
     */
    RECIPIENTS_CHAIN("RECIPIENTS_CHAIN", "接收人-（原）规则链"),
    /**
     * 设备 - （原）规则节点
     */
    DEVICE_NODE("DEVICE", "设备 - 规则节点"),
    /**
     * 感知通道 - （原）规则节点
     */
    SENSING_OBJECT_CHANNEL_NODE("SENSING_OBJECT_CHANNEL_NODE", "感知通道 - 规则节点"),
    /**
     * 接收方 - (二开) 规则节点
     */
    RECIPIENTS_RULE_CHAIN_OWN("RECIPIENTS_RULE_CHAIN_OWN", "接收方 - (二开) 规则节点"),
    /**
     * ROI - (二开) 规则节点
     */
    ROI_RULE_CHAIN_OWN("ROI_RULE_CHAIN_OWN", "ROI - (二开) 规则节点"),
    ;
    private final String value;
    private final String label;

}
