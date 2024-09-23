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

package org.thingsboard.server.dao.model.sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.thingsboard.server.common.data.id.RuleChainOwnId;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * 规则链表（二开） - 数据库对象
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 9:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.RULE_CHAIN_OWN)
public class RuleChainOwnEntity extends BaseSqlEntity<RuleChainOwn> implements SearchTextEntity<RuleChainOwn> {

    /**
     * 租户ID
     */
    @Column(name = ModelConstants.TENANT_ID_COLUMN)
    private UUID tenantId;

    /**
     * rule_chain.id,规则链ID
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_RULE_CHAIN_ID_COLUMN)
    private UUID ruleChainId;

    /**
     * rule_node.id,规则节点ID
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_RULE_NODE_ID_COLUMN)
    private UUID ruleNodeId;

    /**
     * 规则链名称
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_NAME_COLUMN)
    private String name;

    /**
     * 触发器
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_TRIGGER_COLUMN)
    private String trigger;

    /**
     * 动作（多个动作逗号隔开）
     */
    @Column(name = ModelConstants.RULE_CHAIN_OWN_ATIONS_COLUMN)
    private String actions;

    /**
     * 查询条件
     */
    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    /**
     * 不同触发器不同的对象,json
     */
    @Type(type = "json")
    @Column(name = ModelConstants.RULE_CHAIN_OWN_JSON_DATA_COLUMN)
    private Object jsonData;

    public RuleChainOwnEntity(RuleChainOwn ruleChainOwn) {
        if (ruleChainOwn.getId() != null) {
            this.setUuid(ruleChainOwn.getUuidId());
        }
        if (ruleChainOwn.getRuleChainOwnId() != null) {
            this.id = DaoUtil.getId(ruleChainOwn.getRuleChainOwnId());
        }
        this.setCreatedTime(ruleChainOwn.getCreatedTime());
        this.setTenantId(ruleChainOwn.getTenantId());

        this.setName(ruleChainOwn.getName());
        this.setSearchText(ruleChainOwn.getSearchText());
        this.setRuleChainId(ruleChainOwn.getRuleChainId());
        this.setRuleNodeId(ruleChainOwn.getRuleNodeId());

        this.setActions(ruleChainOwn.getActions());
        this.setTrigger(ruleChainOwn.getTrigger());
        this.setJsonData(ruleChainOwn.getJsonData());
    }

    @Override
    public RuleChainOwn toData() {
        RuleChainOwn build = RuleChainOwn.builder()
                .ruleChainOwnId(new RuleChainOwnId(this.getUuid()))
                .tenantId(tenantId)
                .ruleChainId(ruleChainId)
                .ruleNodeId(ruleNodeId)
                .name(name)
                .trigger(trigger)
                .actions(actions)
                .jsonData(jsonData)
                .build();
        build.setCreatedTime(createdTime);
        return build;
    }

    @Override
    public String getSearchTextSource() {
        return searchText;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}