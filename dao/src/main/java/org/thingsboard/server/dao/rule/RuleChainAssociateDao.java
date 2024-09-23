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


import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;
import org.thingsboard.server.dao.Dao;

import java.util.List;
import java.util.UUID;

/**
 * 各模块租户规则链信息关联表 - Dao
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:39
 */
public interface RuleChainAssociateDao extends Dao<RuleChainAssociate> {
    /**
     * 获取租户关联责任链关联
     *
     * @param tenantId 租户ID
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociate> findByTenantId(UUID tenantId);

    /**
     * 删除规则链（二开）关系
     *
     * @param ruleChainOwnId 规则链ID
     * @return boolean
     */
    int deleteByRuleNodeId(UUID ruleChainOwnId);

    /**
     * 删除规则链（二开）关系
     *
     * @param associateId 第三方关联ID
     */
    int deleteByAssociateId(UUID associateId);


    /**
     * 查找规则链对应关系
     *
     * @param ruleId 关联规则ID
     * @param type   类型
     * @param uuids
     * @return {@link RuleChainAssociate}
     */
    List<RuleNodeAssociate> findNodeAssociateByRuleIdAndType(UUID ruleId, String type, List<UUID> uuids);

    /**
     * 查找规则链对应关系
     *
     * @param ruleId 关联规则ID
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociate> findByRuleId(UUID ruleId);

    List<RuleChainAssociate> findByTypeInAndAssociateIdIn(List<String> associateTypeList, List<UUID> uuids);

    List<RuleChainAssociate> findChainTypeList(String chainValue, List<UUID> uuids, UUID id);

    /**
     * 查找AssociateId关系
     *
     * @param associateId 第三方主键
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociate> findByAssociateId(UUID associateId);

    /**
     * 查找接收方
     *
     * @param abilityId 能力ID
     * @return 接收方Id数组
     */
    List<String> findRecipientsByRoi(UUID abilityId);
}

