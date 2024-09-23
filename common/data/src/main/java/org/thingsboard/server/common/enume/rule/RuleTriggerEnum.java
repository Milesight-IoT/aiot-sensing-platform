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
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.enume.IEnum;

/**
 * 自定义规则节点触发器配置
 *
 * @author Luohh
 * @version 1.0.0
 * @date 2023年4月3日16:34:11
 */
@Getter
@AllArgsConstructor
public enum RuleTriggerEnum implements IEnum<String> {
    /**
     * 自定义规则节点触发器配置
     * value:名称
     * label:说明
     * relationType：关系类型
     */
    ONCE_DATA_RECEIVED("Once data received",
            "一旦所选感知对象的通道接收到数据,就对配置的接收方以JSON格式发送该数据",
            true, EntityRelation.TRUE, "Root Once Data Received"),
    LOW_BATTERY("Low battery",
            "一旦所选设备推送的电量信息低于所配置的值,则执行所选的动作(推送数据给接收方或展示在仪表盘组件上)",
            true, EntityRelation.TRUE, "Root Low Battery"),
    DEVICES_BECOME_INACTIVE("Devices become inactive",
            "一旦所选设备从活跃状态变为不活跃,则执行所选动作(推送数据给接收方 或展示在仪表盘组件上)",
            true, EntityRelation.TRUE, "Root Devices Become Inactive"),
    ONCE_RESULT_RECOGNIZED("Once result recognized", "当收到推理平台传的识别结果时会触发该条规则",
            false, EntityRelation.TRUE, "Root Once Result Recognized");

    private final String value;
    private final String label;
    private final Boolean needAddNode;
    private final String relationType;
    /**
     * 规则链名称（固定）
     */
    private final String ruleChainName;
}
