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

package org.thingsboard.server.dao.sql.recipients;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.RecipientsEntity;

import java.util.List;
import java.util.UUID;

/**
 * 接收方网络配置管理 - Repository
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:08
 */
public interface RecipientsRepository extends JpaRepository<RecipientsEntity, UUID> {
    /**
     * 接收方网络配置管理列表
     *
     * @param tenantId   租户ID
     * @param searchText 查询文本
     * @param toPageable 分页参数
     * @return {@link RecipientsEntity}
     */
    @Query("SELECT t FROM RecipientsEntity t "
            + " WHERE t.tenantId = :tenantId "
            + " AND t.searchText LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<RecipientsEntity> findRecipientsList(@Param("tenantId") UUID tenantId,
                                              @Param("searchText") String searchText,
                                              Pageable toPageable);

    /**
     * （MS）根据名称获取详情
     *
     * @param tenantId 租户ID
     * @param name     名称
     * @return {@link RecipientsEntity}
     */
    List<RecipientsEntity> findByTenantIdAndName(UUID tenantId, String name);
}