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

package org.thingsboard.server.common.data.rule.customize;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 低电量节点数据 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/30 13:33
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LowBatteryNodeData extends BaseCustomizeRuleNodeData implements Serializable {
    /**
     * 遥测数据电量（页面属性）
     */
    private Integer threshold;

    /**
     * 遥测数据电量(字段)
     */
    private Integer battery;

    /**
     * 设备ID,多个逗号隔开
     */
    private String deviceIds;
}
