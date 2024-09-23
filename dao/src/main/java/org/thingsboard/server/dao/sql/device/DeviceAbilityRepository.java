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
package org.thingsboard.server.dao.sql.device;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.device.RoiSelectData;
import org.thingsboard.server.dao.model.sql.DeviceAbilityEntity;
import org.thingsboard.server.dao.model.sql.DeviceAbilityInfoEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceAbilityRepository extends JpaRepository<DeviceAbilityEntity, UUID> {

    Page<DeviceAbilityEntity> findDeviceAbilitiesByDeviceId(UUID deviceId, Pageable pageable);

    Page<DeviceAbilityEntity> findDeviceAbilitiesByDeviceIdAndAbilityType(UUID deviceId, short abilityType, Pageable pageable);

    @Modifying
    @Query("update DeviceAbilityEntity set sensingObjectId = null where sensingObjectId = :sensingObjectId")
    int updateSensingObjectIdBySensingObjectId(@Param("sensingObjectId") UUID sensingObjectId);

    @Query("SELECT new org.thingsboard.server.dao.model.sql.DeviceAbilityInfoEntity(dai, d.name) " +
            "FROM DeviceAbilityInfoEntity dai " +
            "INNER JOIN DeviceEntity d on dai.deviceId = d.id " +
            "WHERE dai.id in :deviceAbilityIdList")
    List<DeviceAbilityInfoEntity> findByIdIn(@Param("deviceAbilityIdList") List<UUID> deviceAbilityIdList);

    @Query("SELECT new org.thingsboard.server.dao.model.sql.DeviceAbilityInfoEntity(dai, d.name) " +
            "FROM DeviceAbilityInfoEntity dai " +
            "INNER JOIN DeviceEntity d on dai.deviceId = d.id " +
            "WHERE dai.id = :deviceAbilityId")
    DeviceAbilityInfoEntity findDeviceAbilityInfoById(@Param("deviceAbilityId") UUID deviceAbilityId);

    DeviceAbilityEntity findDeviceAbilityByDeviceIdAndAbility(UUID deviceId, String ability);

    /**
     * 查找数组
     *
     * @param sensingObjectId 感知通道ID
     * @return {@link DeviceAbilityEntity}
     */
    List<DeviceAbilityEntity> findAllBySensingObjectId(UUID sensingObjectId);

    /**
     * 查找数组
     *
     * @param deviceId 设备ID
     * @return {@link DeviceAbilityEntity}
     */
    List<DeviceAbilityEntity> findByDeviceId(UUID deviceId);

    /**
     * 获取设备ROI通道
     *
     * @param deviceIdList 设备ID
     * @return {@link DeviceAbilityEntity}
     */
    @Query(" SELECT dai FROM DeviceAbilityEntity dai " +
            " WHERE dai.deviceId in :deviceIdList and dai.abilityType = 2 and dai.sensingObjectId is not null and dai.ability <> 'image' ")
    List<DeviceAbilityEntity> findImageByDeviceIds(@Param("deviceIdList") List<UUID> deviceIdList);

    /**
     * 获取ROI下拉列表
     *
     * @param tenantId    租户ID
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param isRoi       是否只需要ROI
     * @param textSearch  查询条件
     * @param toPageable  分页信息toPageable
     * @return {@link RoiSelectData}
     */
    @Query(value = "SELECT "
            + " CAST(d.id AS VARCHAR) deviceId, "
            + " d.NAME deviceName, "
            + " array_to_string( ARRAY_AGG ( dai.ability ), ',' ) abilityList , "
            + " array_to_string( ARRAY_AGG ( CAST(dai.id AS VARCHAR) ), ',' ) abilityIdList  "
            + "FROM device d "
            + " INNER JOIN device_ability dai ON d.ID = dai.device_id AND (:abilityType = 0 OR dai.ability_type = :abilityType) AND (:isRoi = false OR dai.ability <> 'image') "
            + " WHERE d.tenant_id = :tenantId "
            + " AND d.search_text LIKE LOWER(CONCAT('%', :textSearch, '%'))"
            + "GROUP BY d.ID", nativeQuery = true)
    Page<Map<String, String>> findRoiSelectList(@Param("tenantId") UUID tenantId, @Param("abilityType") Short abilityType,
                                                @Param("isRoi") boolean isRoi, @Param("textSearch") String textSearch, Pageable toPageable);

    /**
     * 获取设备-ROI下拉详情列表
     *
     * @param tenantId     租户ID
     * @param deviceIdList 设备ID
     * @param isRoi        是否只需要ROI
     * @param abilityType  能力类型 0-所有; 1-基础能力; 2-图片能力
     * @return {@link RoiSelectData}
     */
    @Query(value = "SELECT "
            + " CAST(d.id AS VARCHAR) deviceId, "
            + " d.NAME deviceName, "
            + " array_to_string( ARRAY_AGG ( dai.ability ), ',' ) abilityList , "
            + " array_to_string( ARRAY_AGG ( CAST(dai.id AS VARCHAR) ), ',' ) abilityIdList  "
            + "FROM device d "
            + " INNER JOIN device_ability dai ON d.ID = dai.device_id AND (:abilityType = 0 OR dai.ability_type = :abilityType) AND (:isRoi = false OR dai.ability <> 'image') "
            + " WHERE d.tenant_id = :tenantId AND d.id in (:deviceIdList) "
            + "GROUP BY d.ID", nativeQuery = true)
    List<Map<String, String>> findRoiSelectByDeviceList(@Param("tenantId") UUID tenantId, @Param("abilityType") Short abilityType,
                                                        @Param("isRoi") boolean isRoi, @Param("deviceIdList") List<UUID> deviceIdList);
}
