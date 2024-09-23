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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.common.enume.rule.RuleChainAssociateTypeEnum;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.RuleChainOwnEntity;
import org.thingsboard.server.dao.rule.RuleChainOwnDao;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

;

/**
 * 规则链表（二开） - JpaDao
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/3 10:55
 */
@Component
@Slf4j
public class JpaRuleChainOwnDao extends JpaAbstractSearchTextDao<RuleChainOwnEntity, RuleChainOwn> implements RuleChainOwnDao {
    @Resource
    private RuleChainOwnRepository repository;

    @Override
    protected Class<RuleChainOwnEntity> getEntityClass() {
        return RuleChainOwnEntity.class;
    }

    @Override
    protected JpaRepository<RuleChainOwnEntity, UUID> getRepository() {
        return repository;
    }

    @Override
    public PageData<RuleChainOwn> findTenantRuleChainOwnList(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(
                repository.findTenantRuleChainOwnList(tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink))
        );
    }

    @Override
    public RuleChainOwn getByTenantIdAndName(UUID tenantId, String name) {
        List<RuleChainOwn> ruleChainOwns = DaoUtil.convertDataList(repository.findByTenantIdAndName(tenantId, name));
        return CollectionUtils.isEmpty(ruleChainOwns) ? null : ruleChainOwns.get(0);
    }

    @Override
    public List<RuleChainOwn> findRuleChainOwnByRecipientId(RecipientsId recipientsId) {
        String value = RuleChainAssociateTypeEnum.RECIPIENTS_RULE_CHAIN_OWN.getValue();
        return DaoUtil.convertDataList(repository.findRuleChainOwnByRecipientId(recipientsId.getId(), value));
    }
}