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

package org.thingsboard.server.dao.rule;

import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;

import java.util.List;
import java.util.UUID;

/**
 * 初始化租户规则链信息关联 - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:33
 */
public interface RuleChainAssociateService {
    /**
     * 删除规则链（二开）关系
     *
     * @param ruleId 规则链/节点ID
     */
    void deleteByRuleId(UUID ruleId);

    /**
     * 删除规则链（二开）关系
     *
     * @param associateId 第三方关联ID
     */
    void deleteByAssociateId(UUID associateId);

    /**
     * 批量保存
     *
     * @param ruleChainAssociates 对象数组
     */
    void saveAll(List<RuleChainAssociate> ruleChainAssociates);

    /**
     * 查找规则链对应关系
     *
     * @param ruleId          关联规则ID
     * @param type            类型
     * @param associateIdList 关联其他表ID
     * @return {@link RuleChainAssociate}
     */
    List<RuleNodeAssociate> findNodeAssociateByRuleIdAndType(UUID ruleId, String type, List<String> associateIdList);

    /**
     * 查找规则链对应关系
     *
     * @param ruleNodeId 关联规则ID
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociate> findByRuleId(RuleNodeId ruleNodeId);

    List<RuleNodeAssociate> findRecipientsByAssociateIdIn(List<String> effectiveRecipientsIdList, String value, RuleChainId ruleChainId);

    /**
     * 查找第三方节点关系
     *
     * @param recipientsId 第三方主键
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociate> findByRecipientsId(RecipientsId recipientsId);

    /**
     * 查找接收方
     *
     * @param abilityId 能力ID
     * @return 接收方Id数组
     */
    List<String> findRecipientsByRoi(UUID abilityId);
}
