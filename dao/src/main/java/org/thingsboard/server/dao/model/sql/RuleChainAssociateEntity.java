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
import org.thingsboard.server.common.data.rule.RuleChainAssociate;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * 各模块租户规则链信息关联表 - 数据库对象
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
@Table(name = ModelConstants.RULE_CHAIN_ASSOCIATE)
public final class RuleChainAssociateEntity extends BaseSqlEntity<RuleChainAssociate> {
    /**
     * 租户ID
     */
    @Column(name = ModelConstants.TENANT_ID_COLUMN)
    private UUID tenantId;

    /**
     * 关联主键ID
     */
    @Column(name = ModelConstants.RULE_CHAIN_ASSOCIATE_ASSOCIATE_ID_COLUMN)
    private UUID associateId;

    /**
     * 关联类型
     */
    @Column(name = ModelConstants.RULE_CHAIN_ASSOCIATE_TYPE_COLUMN)
    private String type;

    /**
     * 规则链节点/规则链ID
     */
    @Column(name = ModelConstants.RULE_CHAIN_ASSOCIATE_RULE_ID_COLUMN)
    private UUID ruleId;

    public RuleChainAssociateEntity(RuleChainAssociate ruleChainAssociate) {
        this.id = ruleChainAssociate.getId();
        this.createdTime = ruleChainAssociate.getCreatedTime();
        this.type = ruleChainAssociate.getType();
        this.associateId = ruleChainAssociate.getAssociateId();
        this.ruleId = ruleChainAssociate.getRuleId();
        this.tenantId = ruleChainAssociate.getTenantId();
    }

    @Override
    public RuleChainAssociate toData() {
        return RuleChainAssociate.builder()
                .id(id)
                .createdTime(createdTime)
                .type(type)
                .associateId(associateId)
                .ruleId(ruleId)
                .tenantId(tenantId)
                .build();
    }
}