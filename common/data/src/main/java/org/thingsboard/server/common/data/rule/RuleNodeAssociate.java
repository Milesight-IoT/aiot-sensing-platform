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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author Luohh
 */
@ApiModel
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleNodeAssociate {

    /**
     * 关联ID
     * recipients主键
     */
    private UUID associateId;
    /**
     * 责任链ID
     */
    private UUID ruleChainId;

    /**
     * 规则链节点ID
     */
    private UUID ruleNodeId;

}
