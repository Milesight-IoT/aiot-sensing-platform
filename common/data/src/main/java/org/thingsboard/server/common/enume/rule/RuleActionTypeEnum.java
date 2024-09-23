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
 * 规则链动作-枚举
 *
 * @author Luohh
 * @version 1.0.0
 * @date 2023年4月3日16:34:11
 */
@Getter
@AllArgsConstructor
public enum RuleActionTypeEnum implements IEnum<String> {
    /**
     * 规则链动作
     */
    SEND_TO_RECIPIENTS("Send to recipients", "发送给收件人"),
    SHOW_ON_WIDGET("Show on widget", "在部件上显示"),
    ;

    private final String value;
    private final String label;
}
