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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.OtaPackageInfoEntity;
import org.thingsboard.server.dao.ota.OtaPackageInfoDao;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@SqlDao
public class JpaOtaPackageInfoDao extends JpaAbstractSearchTextDao<OtaPackageInfoEntity, OtaPackageInfo> implements OtaPackageInfoDao {

    @Autowired
    private OtaPackageInfoRepository otaPackageInfoRepository;

    @Override
    protected Class<OtaPackageInfoEntity> getEntityClass() {
        return OtaPackageInfoEntity.class;
    }

    @Override
    protected JpaRepository<OtaPackageInfoEntity, UUID> getRepository() {
        return otaPackageInfoRepository;
    }

    @Override
    public OtaPackageInfo findById(TenantId tenantId, UUID id) {
        return DaoUtil.getData(otaPackageInfoRepository.findOtaPackageInfoById(id));
    }

    @Override
    public OtaPackageInfo save(TenantId tenantId, OtaPackageInfo otaPackageInfo) {
        OtaPackageInfo savedOtaPackage = super.save(tenantId, otaPackageInfo);
        if (otaPackageInfo.getId() == null) {
            return savedOtaPackage;
        } else {
            return findById(tenantId, savedOtaPackage.getId().getId());
        }
    }

    @Override
    public PageData<OtaPackageInfo> findOtaPackageInfoByTenantId(TenantId tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(otaPackageInfoRepository
                .findAllByTenantId(
                        tenantId.getId(),
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<OtaPackageInfo> findOtaPackageInfoByTenantIdAndDeviceProfileIdAndTypeAndHasData(TenantId tenantId, DeviceProfileId deviceProfileId, OtaPackageType otaPackageType, PageLink pageLink) {
        return DaoUtil.toPageData(otaPackageInfoRepository
                .findAllByTenantIdAndTypeAndDeviceProfileIdAndHasData(
                        tenantId.getId(),
                        deviceProfileId.getId(),
                        otaPackageType,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public boolean isOtaPackageUsed(OtaPackageId otaPackageId, OtaPackageType otaPackageType, DeviceProfileId deviceProfileId) {
        return otaPackageInfoRepository.isOtaPackageUsed(otaPackageId.getId(), deviceProfileId.getId(), otaPackageType.name());
    }

    @Override
    public OtaPackageInfo findFirmwareByDeviceId(UUID deviceId) {
        return DaoUtil.getData(otaPackageInfoRepository.findFirmwareByDeviceId(deviceId));
    }

    @Override
    public OtaPackageInfo findConfigureByDeviceId(UUID deviceId) {
        return DaoUtil.getData(otaPackageInfoRepository.findConfigureByDeviceId(deviceId));
    }

    @Override
    public OtaPackageInfo findByTenantIdAndTitleAndVersion(UUID tenantId, String title, String version) {
        return DaoUtil.getData(otaPackageInfoRepository.findByTenantIdAndTitleAndVersion(tenantId, title, version));
    }

    @Override
    public OtaPackageInfo findByTenantIdAndTitle(UUID tenantId, String title) {
        return DaoUtil.getData(otaPackageInfoRepository.findByTenantIdAndTitle(tenantId, title));
    }

    @Override
    public OtaPackageInfo findFirmwareByCredentialsId(String credentialsId) {
        return DaoUtil.getData(otaPackageInfoRepository.findFirmwareByCredentialsId(credentialsId));
    }

    @Override
    public OtaPackageInfo findConfigureByCredentialsId(String credentialsId) {
        return DaoUtil.getData(otaPackageInfoRepository.findConfigureByCredentialsId(credentialsId));
    }
}
