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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.common.data.rule.RuleNodeAssociate;
import org.thingsboard.server.common.data.util.CollectionsUtil;
import org.thingsboard.server.common.enume.rule.RuleChainAssociateTypeEnum;
import org.thingsboard.server.dao.BaseDaoServiceImpl;
import org.thingsboard.server.dao.DaoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 初始化租户规则链信息关联 - 服务实现
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:35
 */
@Slf4j
@Service
public class RuleChainAssociateServiceImpl extends BaseDaoServiceImpl<RuleChainAssociateDao, RuleChainAssociate>
        implements RuleChainAssociateService {

    @Override
    public void deleteByRuleId(UUID ruleId) {
        baseDao.deleteByRuleNodeId(ruleId);
    }

    @Override
    public void deleteByAssociateId(UUID associateId) {
        baseDao.deleteByAssociateId(associateId);
    }

    @Override
    public void saveAll(List<RuleChainAssociate> ruleChainAssociates) {
        baseDao.saveAll(ruleChainAssociates);
    }

    @Override
    public List<RuleNodeAssociate> findNodeAssociateByRuleIdAndType(UUID ruleId, String type, List<String> associateIdList) {
        if (CollectionsUtil.isEmpty(associateIdList)) {
            log.info("No data associateIdList");
            return Collections.emptyList();
        }
        return baseDao.findNodeAssociateByRuleIdAndType(ruleId, type, DaoUtil.toUUIDsFromString(associateIdList));
    }

    @Override
    public List<RuleChainAssociate> findByRuleId(RuleNodeId ruleNodeId) {
        return baseDao.findByRuleId(ruleNodeId.getId());
    }

    @Override
    public List<RuleNodeAssociate> findRecipientsByAssociateIdIn(List<String> associateIdList, String associateType, RuleChainId ruleChainId) {
        if (CollectionsUtil.isEmpty(associateIdList)) {
            log.info("No data associateIdList");
            return Collections.emptyList();
        }
        List<UUID> uuids = DaoUtil.toUUIDsFromString(associateIdList);
        List<RuleChainAssociate> list = baseDao.findByTypeInAndAssociateIdIn(Collections.singletonList(associateType), uuids);
        String chainValue = RuleChainAssociateTypeEnum.RECIPIENTS_CHAIN.getValue();
        List<RuleChainAssociate> typeList = baseDao.findChainTypeList(chainValue, uuids, ruleChainId.getId());
        list.addAll(typeList);
        if (CollectionsUtil.isEmpty(list)) {
            log.info("No data associateIdList");
            return Collections.emptyList();
        }
        List<RuleNodeAssociate> ruleNodeAssociates = new ArrayList<>();
        list.stream().collect(Collectors.groupingBy(RuleChainAssociate::getAssociateId))
                .forEach((uuid, ruleChainAssociates) -> {
                    RuleNodeAssociate ruleNodeAssociate = new RuleNodeAssociate();
                    ruleNodeAssociate.setAssociateId(uuid);
                    ruleChainAssociates.forEach(ruleChainAssociate -> {
                        String type = ruleChainAssociate.getType();
                        if (RuleChainAssociateTypeEnum.RECIPIENTS_CHAIN.eq(type)) {
                            ruleNodeAssociate.setRuleChainId(ruleChainAssociate.getRuleId());
                        } else {
                            ruleNodeAssociate.setRuleNodeId(ruleChainAssociate.getRuleId());
                        }
                    });
                    ruleNodeAssociates.add(ruleNodeAssociate);
                });
        return ruleNodeAssociates;
    }

    @Override
    public List<RuleChainAssociate> findByRecipientsId(RecipientsId recipientsId) {
        return baseDao.findByAssociateId(recipientsId.getId());
    }

    @Override
    public List<String> findRecipientsByRoi(UUID abilityId) {
        List<String> recipientsByRoi = baseDao.findRecipientsByRoi(abilityId);
        if(CollectionUtils.isEmpty(recipientsByRoi)){
            return Collections.emptyList();
        }
        return recipientsByRoi;
    }

}
