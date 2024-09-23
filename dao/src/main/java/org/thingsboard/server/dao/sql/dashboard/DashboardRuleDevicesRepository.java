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

package org.thingsboard.server.dao.sql.dashboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.DashboardRuleDevicesEntity;

import java.util.UUID;

/**
 * @author zhangzy
 * @Date 2022/04/19
 */
public interface DashboardRuleDevicesRepository extends JpaRepository<DashboardRuleDevicesEntity, UUID> {

    /**
     * 查找MS Dashboard 列表数据
     *
     * @param tenantId    租户ID
     * @param createdTime 创建时间
     * @param pageable    分页数据
     * @return {@link DashboardRuleDevicesEntity}
     */
    @Query("SELECT di FROM DashboardRuleDevicesEntity di "
            + " WHERE di.tenantId = :tenantId"
            + " AND di.createdTime >= :createdTime")
    Page<DashboardRuleDevicesEntity> findByTenantIdAndTime(@Param("tenantId") UUID tenantId, @Param("createdTime") Long createdTime,
                                                           Pageable pageable);
}
