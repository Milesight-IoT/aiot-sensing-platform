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

package org.thingsboard.server.dao.sql.rule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;
import org.thingsboard.server.dao.model.sql.RuleChainAssociateEntity;

import java.util.List;
import java.util.UUID;

/**
 * 各模块租户规则链信息关联表 - Repository
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:49
 */
public interface RuleChainAssociateRepository extends JpaRepository<RuleChainAssociateEntity, UUID> {
    /**
     * 获取租户关联责任链关联
     *
     * @param tenantId 租户ID
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociateEntity> findByTenantId(UUID tenantId);

    /**
     * 删除关系
     *
     * @param ruleNodeId 规则节点ID
     * @return 是否删除成功
     */
    int deleteAllByRuleId(UUID ruleNodeId);

    /**
     * 删除关系
     *
     * @param associateId 第三方关联ID
     * @return 是否删除成功
     */
    int deleteAllByAssociateId(UUID associateId);

    /**
     * 查找关系和规则节点
     *
     * @param ruleId 规则链
     * @param type   类型
     * @param uuids  associateId集合
     * @return 所有
     */
    @Query("SELECT new org.thingsboard.server.common.data.rule.RuleNodeAssociate(rc.associateId, d.ruleChainId, d.id) "
            + "FROM RuleChainAssociateEntity rc "
            + "INNER JOIN RuleNodeEntity d on rc.associateId = d.id "
            + "WHERE rc.ruleId = :ruleId AND rc.type = :type AND rc.associateId IN (:uuids)")
    List<RuleNodeAssociate> findNodeAssociateByRuleIdAndType(@Param("ruleId") UUID ruleId, @Param("type") String type,
                                                             @Param("uuids") List<UUID> uuids);

    /**
     * 查找关系和规则节点
     *
     * @param ruleId 规则链
     * @return 所有
     */
    List<RuleChainAssociateEntity> findAllByRuleId(UUID ruleId);

    List<RuleChainAssociateEntity> findByTypeInAndAssociateIdIn(List<String> associateTypeList, List<UUID> uuids);

    List<RuleChainAssociateEntity> findByTypeAndAssociateIdInAndRuleId(String chainValue, List<UUID> uuids, UUID ruleChainId);

    /**
     * 查找AssociateId关系
     *
     * @param associateId 第三方主键
     * @return {@link RuleChainAssociate}
     */
    List<RuleChainAssociateEntity> findByAssociateId(UUID associateId);

    /**
     * 查找接收方
     *
     * @param abilityId 能力ID
     * @return list
     */
    @Query("SELECT DISTINCT rca.associateId "
            + "FROM RuleChainAssociateEntity rc "
            + "INNER JOIN RuleChainAssociateEntity rca on rc.ruleId = rca.ruleId and rca.type = 'RECIPIENTS_RULE_CHAIN_OWN' "
            + "WHERE rc.associateId = :abilityId and rc.type = 'ROI_RULE_CHAIN_OWN'")
    List<String> findRecipientsByRoi(@Param("abilityId") UUID abilityId);

}