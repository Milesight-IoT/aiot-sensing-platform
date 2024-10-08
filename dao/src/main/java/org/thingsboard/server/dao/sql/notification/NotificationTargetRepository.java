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
package org.thingsboard.server.dao.sql.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sql.NotificationTargetEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationTargetRepository extends JpaRepository<NotificationTargetEntity, UUID> {

    @Query("SELECT t FROM NotificationTargetEntity t WHERE t.tenantId = :tenantId " +
            "AND (:searchText = '' OR lower(t.name) LIKE lower(concat('%', :searchText, '%')))")
    Page<NotificationTargetEntity> findByTenantIdAndSearchText(@Param("tenantId") UUID tenantId,
                                                               @Param("searchText") String searchText,
                                                               Pageable pageable);

    @Query(value = "SELECT * FROM notification_target t WHERE t.tenant_id = :tenantId " +
            "AND (:searchText = '' OR lower(t.name) LIKE lower(concat('%', :searchText, '%'))) " +
            "AND (cast(t.configuration as json) ->> 'type' <> 'PLATFORM_USERS' OR " +
            "cast(t.configuration as json) -> 'usersFilter' ->> 'type' IN :usersFilterTypes)", nativeQuery = true)
    Page<NotificationTargetEntity> findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(@Param("tenantId") UUID tenantId,
                                                                                          @Param("searchText") String searchText,
                                                                                          @Param("usersFilterTypes") List<String> usersFilterTypes,
                                                                                          Pageable pageable);

    List<NotificationTargetEntity> findByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);

    @Transactional
    void deleteByTenantId(UUID tenantId);

    long countByTenantId(UUID tenantId);

}
