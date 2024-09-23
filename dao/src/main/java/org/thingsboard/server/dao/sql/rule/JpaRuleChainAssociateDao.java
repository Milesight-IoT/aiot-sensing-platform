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

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.RuleChainAssociateEntity;
import org.thingsboard.server.dao.rule.RuleChainAssociateDao;
import org.thingsboard.server.dao.sql.JpaAbstractDao;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * 各模块租户规则链信息关联表 - JpaDao
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:50
 */
@Component
@Slf4j
public class JpaRuleChainAssociateDao extends JpaAbstractDao<RuleChainAssociateEntity, RuleChainAssociate> implements RuleChainAssociateDao {

    @Resource
    private RuleChainAssociateRepository repository;

    @Override
    protected Class<RuleChainAssociateEntity> getEntityClass() {
        return RuleChainAssociateEntity.class;
    }

    @Override
    protected JpaRepository<RuleChainAssociateEntity, UUID> getRepository() {
        return repository;
    }

    @Override
    public List<RuleChainAssociate> findByTenantId(UUID tenantId) {
        return DaoUtil.convertDataList(repository.findByTenantId(tenantId));
    }

    @Override
    public int deleteByRuleNodeId(UUID ruleNodeId) {
        return repository.deleteAllByRuleId(ruleNodeId);
    }

    @Override
    public int deleteByAssociateId(UUID associateId) {
        return repository.deleteAllByAssociateId(associateId);
    }

    @Override
    public List<RuleNodeAssociate> findNodeAssociateByRuleIdAndType(UUID ruleId, String type, List<UUID> uuids) {
        return repository.findNodeAssociateByRuleIdAndType(ruleId, type, uuids);
    }

    @Override
    public List<RuleChainAssociate> findByRuleId(UUID ruleId) {
        return DaoUtil.convertDataList(repository.findAllByRuleId(ruleId));
    }

    @Override
    public List<RuleChainAssociate> findByTypeInAndAssociateIdIn(List<String> associateTypeList, List<UUID> uuids) {
        return DaoUtil.convertDataList(repository.findByTypeInAndAssociateIdIn(associateTypeList, uuids));
    }

    @Override
    public List<RuleChainAssociate> findChainTypeList(String chainValue, List<UUID> uuids, UUID ruleChainId) {
        return DaoUtil.convertDataList(repository.findByTypeAndAssociateIdInAndRuleId(chainValue, uuids, ruleChainId));
    }

    @Override
    public List<RuleChainAssociate> findByAssociateId(UUID associateId) {
        return DaoUtil.convertDataList(repository.findByAssociateId(associateId));
    }

    @Override
    public List<String> findRecipientsByRoi(UUID abilityId) {
        return repository.findRecipientsByRoi(abilityId);
    }

}
