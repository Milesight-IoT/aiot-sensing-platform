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
package org.thingsboard.server.dao.sql.sensing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.SensingObjectEntity;

import java.util.List;
import java.util.UUID;

public interface SensingObjectRepository extends JpaRepository<SensingObjectEntity, UUID> {

    @Query("SELECT so FROM SensingObjectEntity so WHERE so.tenantId = :tenantId " +
            "AND LOWER(so.name) LIKE LOWER(CONCAT('%', :textSearch, '%'))")
    Page<SensingObjectEntity> findByTenantId(@Param("tenantId") UUID tenantId,
                                             @Param("textSearch") String textSearch,
                                             Pageable pageable);

    @Query("SELECT so FROM DeviceEntity d " +
            "LEFT JOIN DeviceAbilityEntity da on d.id = da.deviceId " +
            "LEFT JOIN SensingObjectEntity so on da.sensingObjectId = so.id " +
            "WHERE d.id = :deviceId ")
    List<SensingObjectEntity> findByDeviceId(@Param("deviceId") UUID deviceId);

    @Query("SELECT so FROM SensingObjectEntity so "
            + " INNER JOIN DeviceAbilityEntity da on so.id = da.sensingObjectId "
            + " WHERE so.tenantId = :tenantId AND LOWER(so.name) LIKE LOWER(CONCAT('%', :textSearch, '%'))"
            + " AND da.abilityType = 2 and (:isRoi = false OR da.ability <> 'image')"
            + " group by so.id ")
    Page<SensingObjectEntity> findByDeviceAbilityIdsImage(@Param("tenantId") UUID tenantId,
                                                          @Param("textSearch") String textSearch,
                                                          @Param("isRoi") boolean isRoi, Pageable pageable);

    @Query("SELECT so FROM DeviceAbilityEntity da " +
            "LEFT JOIN SensingObjectEntity so on da.sensingObjectId = so.id " +
            "WHERE da.id = :deviceAbilityId ")
    SensingObjectEntity findByDeviceAbilityId(@Param("deviceAbilityId") UUID deviceAbilityId);

    SensingObjectEntity findByTenantIdAndName(UUID tenantId, String name);

    /**
     * 批量查找感知
     *
     * @param uuidList 主键ID
     * @return {@link SensingObjectEntity}
     */
    List<SensingObjectEntity> findByIdIn(List<UUID> uuidList);
}
