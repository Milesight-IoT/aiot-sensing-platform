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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.dao.model.sql.RuleChainOwnEntity;

import java.util.List;
import java.util.UUID;

/**
 * 规则链表（二开） - Repository
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/3 10:53
 */
public interface RuleChainOwnRepository extends JpaRepository<RuleChainOwnEntity, UUID> {
    /**
     * 规则链（二开）列表
     *
     * @param tenantId   租户ID
     * @param searchText 查询文本
     * @param toPageable 分页参数
     * @return {@link RuleChainOwnEntity}
     */
    @Query("SELECT rc FROM RuleChainOwnEntity rc "
            + " WHERE rc.tenantId = :tenantId "
            + " AND rc.searchText LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<RuleChainOwnEntity> findTenantRuleChainOwnList(@Param("tenantId") UUID tenantId,
                                                        @Param("searchText") String searchText,
                                                        Pageable toPageable);

    /**
     * 根据名称获取
     *
     * @param tenantId 租户ID
     * @param name     名称
     * @return {@link RuleChainOwnEntity}
     */
    List<RuleChainOwnEntity> findByTenantIdAndName(UUID tenantId, String name);

    /**
     * （MS）获取第三方关联规则链列表
     *
     * @param recipientsId 第三方主键
     * @param type         类型
     * @return {@link RuleChainOwn}
     */
    @Query("SELECT rc FROM RuleChainOwnEntity rc "
            + " INNER JOIN RuleChainAssociateEntity ra on rc.id = ra.ruleId "
            + " WHERE ra.associateId = :recipientsId and ra.type = :type")
    List<RuleChainOwnEntity> findRuleChainOwnByRecipientId(@Param("recipientsId") UUID recipientsId,@Param("type") String type);
}