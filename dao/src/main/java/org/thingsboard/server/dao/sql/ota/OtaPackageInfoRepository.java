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
package org.thingsboard.server.dao.sql.ota;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.dao.model.sql.OtaPackageInfoEntity;

import java.util.UUID;

public interface OtaPackageInfoRepository extends JpaRepository<OtaPackageInfoEntity, UUID> {
    @Query("SELECT new OtaPackageInfoEntity(f.id, f.createdTime, f.tenantId, f.deviceProfileId, f.type, f.title, f.version, f.tag, f.url, f.fileName, f.contentType, f.checksumAlgorithm, f.checksum, f.dataSize, f.additionalInfo, CASE WHEN (f.data IS NOT NULL OR f.url IS NOT NULL)  THEN true ELSE false END, dp.name, f.distributeStatus) FROM OtaPackageEntity f " +
            "LEFT JOIN DeviceProfileEntity AS dp ON dp.id = f.deviceProfileId " +
            "WHERE f.tenantId = :tenantId " +
            "AND LOWER(f.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<OtaPackageInfoEntity> findAllByTenantId(@Param("tenantId") UUID tenantId,
                                                 @Param("searchText") String searchText,
                                                 Pageable pageable);

    @Query("SELECT new OtaPackageInfoEntity(f.id, f.createdTime, f.tenantId, f.deviceProfileId, f.type, f.title, f.version, f.tag, f.url, f.fileName, f.contentType, f.checksumAlgorithm, f.checksum, f.dataSize, f.additionalInfo, true, f.distributeStatus) FROM OtaPackageEntity f WHERE " +
            "f.tenantId = :tenantId " +
            "AND f.deviceProfileId = :deviceProfileId " +
            "AND f.type = :type " +
            "AND (f.data IS NOT NULL OR f.url IS NOT NULL) " +
            "AND LOWER(f.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<OtaPackageInfoEntity> findAllByTenantIdAndTypeAndDeviceProfileIdAndHasData(@Param("tenantId") UUID tenantId,
                                                                                    @Param("deviceProfileId") UUID deviceProfileId,
                                                                                    @Param("type") OtaPackageType type,
                                                                                    @Param("searchText") String searchText,
                                                                                    Pageable pageable);

    @Query("SELECT new OtaPackageInfoEntity(f.id, f.createdTime, f.tenantId, f.deviceProfileId, f.type, f.title, f.version, f.tag, f.url, f.fileName, f.contentType, f.checksumAlgorithm, f.checksum, f.dataSize, f.additionalInfo, CASE WHEN (f.data IS NOT NULL OR f.url IS NOT NULL)  THEN true ELSE false END, f.distributeStatus) FROM OtaPackageEntity f WHERE f.id = :id")
    OtaPackageInfoEntity findOtaPackageInfoById(@Param("id") UUID id);

    @Query(value = "SELECT exists(SELECT * " +
            "FROM device_profile AS dp " +
            "LEFT JOIN device AS d ON dp.id = d.device_profile_id " +
            "WHERE dp.id = :deviceProfileId AND " +
            "(('FIRMWARE' = :type AND (dp.firmware_id = :otaPackageId OR d.firmware_id = :otaPackageId)) " +
            "OR ('SOFTWARE' = :type AND (dp.software_id = :otaPackageId or d.software_id = :otaPackageId))))", nativeQuery = true)
    boolean isOtaPackageUsed(@Param("otaPackageId") UUID otaPackageId, @Param("deviceProfileId") UUID deviceProfileId, @Param("type") String type);

//    @Query("select new OtaPackageInfoEntity(f.id, f.createdTime, f.tenantId, f.deviceProfileId, f.type, f.title, f.version, f.tag, f.url, f.fileName, f.contentType, f.checksumAlgorithm, f.checksum, f.dataSize, f.additionalInfo, CASE WHEN (f.data IS NOT NULL OR f.url IS NOT NULL)  THEN true ELSE false END, f.distributeStatus) " +
//            "from OtaPackageInfoEntity f left join DeviceEntity d on f.id = d.firmwareId where d.id = :deviceId")
//    OtaPackageInfoEntity findFirmwareByDeviceId(@Param("deviceId") UUID deviceId);

    @Query(value = "select f.* from ota_package f left join device d on f.id = d.firmware_id where d.id = :deviceId", nativeQuery = true)
    OtaPackageInfoEntity findFirmwareByDeviceId(@Param("deviceId") UUID deviceId);

    @Query(value = "select f.* from ota_package f left join device d on f.id = d.configure_id where d.id = :deviceId", nativeQuery = true)
    OtaPackageInfoEntity findConfigureByDeviceId(@Param("deviceId") UUID deviceId);

    OtaPackageInfoEntity findByTenantIdAndTitleAndVersion(UUID tenantId, String title, String version);

    OtaPackageInfoEntity findByTenantIdAndTitle(UUID tenantId, String title);

    @Query(value = "select f.* from ota_package f " +
            "left join device d on f.id = d.firmware_id " +
            "left join device_credentials dc on d.id = dc.device_id " +
            "where dc.credentials_id = :credentialsId", nativeQuery = true)
    OtaPackageInfoEntity findFirmwareByCredentialsId(@Param("credentialsId") String credentialsId);

    @Query(value = "select f.* from ota_package f " +
            "left join device d on f.id = d.configure_id " +
            "left join device_credentials dc on d.id = dc.device_id " +
            "where dc.credentials_id = :credentialsId", nativeQuery = true)
    OtaPackageInfoEntity findConfigureByCredentialsId(@Param("credentialsId") String credentialsId);
}
