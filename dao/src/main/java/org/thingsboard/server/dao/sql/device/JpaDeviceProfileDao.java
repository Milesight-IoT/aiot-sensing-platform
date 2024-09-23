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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.DeviceProfileInfo;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.enume.device.ProfileTypeEnum;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.device.DeviceProfileDao;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@SqlDao
public class JpaDeviceProfileDao extends JpaAbstractSearchTextDao<DeviceProfileEntity, DeviceProfile> implements DeviceProfileDao {

    @Autowired
    private DeviceProfileRepository deviceProfileRepository;

    @Override
    protected Class<DeviceProfileEntity> getEntityClass() {
        return DeviceProfileEntity.class;
    }

    @Override
    protected JpaRepository<DeviceProfileEntity, UUID> getRepository() {
        return deviceProfileRepository;
    }

    @Override
    public DeviceProfileInfo findDeviceProfileInfoById(TenantId tenantId, UUID deviceProfileId) {
        return deviceProfileRepository.findDeviceProfileInfoById(deviceProfileId);
    }

    @Transactional
    @Override
    public DeviceProfile saveAndFlush(TenantId tenantId, DeviceProfile deviceProfile) {
        DeviceProfile result = save(tenantId, deviceProfile);
        deviceProfileRepository.flush();
        return result;
    }

    @Override
    public PageData<DeviceProfile> findDeviceProfiles(TenantId tenantId, PageLink pageLink) {
        // 设备型号类型,默认0,新增字段 0 - 自定义型号（当前租户可见）1 - 系统型号（所有租户共享）
        return DaoUtil.toPageData(
                deviceProfileRepository.findDeviceProfiles(
                        tenantId.getId(),
                        Objects.toString(pageLink.getTextSearch(), ""),
                        ProfileTypeEnum.SYSTEM_MODEL.getValue(),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<DeviceProfile> findDeleteDeviceProfiles(TenantId tenantId, PageLink pageLink, Short profileType) {
        return DaoUtil.toPageData(
                deviceProfileRepository.findDeleteDeviceProfiles(
                        tenantId.getId(),
                        profileType,
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<DeviceProfileInfo> findDeviceProfileInfos(TenantId tenantId, PageLink pageLink, String transportType) {
        if (StringUtils.isNotEmpty(transportType)) {
            return DaoUtil.pageToPageData(
                    deviceProfileRepository.findDeviceProfileInfos(
                            tenantId.getId(),
                            Objects.toString(pageLink.getTextSearch(), ""),
                            DeviceTransportType.valueOf(transportType),
                            DaoUtil.toPageable(pageLink)));
        } else {
            return DaoUtil.pageToPageData(
                    deviceProfileRepository.findDeviceProfileInfos(
                            tenantId.getId(),
                            Objects.toString(pageLink.getTextSearch(), ""),
                            DaoUtil.toPageable(pageLink)));
        }
    }

    @Override
    public DeviceProfile findDefaultDeviceProfile(TenantId tenantId) {
        return DaoUtil.getData(deviceProfileRepository.findByDefaultTrueAndTenantId(tenantId.getId()));
    }

    @Override
    public DeviceProfileInfo findDefaultDeviceProfileInfo(TenantId tenantId) {
        return deviceProfileRepository.findDefaultDeviceProfileInfo(tenantId.getId());
    }

    @Override
    public DeviceProfile findByProvisionDeviceKey(String provisionDeviceKey) {
        return DaoUtil.getData(deviceProfileRepository.findByProvisionDeviceKey(provisionDeviceKey));
    }

    @Override
    public DeviceProfile findByName(TenantId tenantId, String profileName) {
        return DaoUtil.getData(deviceProfileRepository.findByTenantIdAndName(tenantId.getId(), profileName));
    }

    @Override
    public DeviceProfile updateDeviceProfileOtaId(UUID deviceProfileId, OtaPackageType type, UUID otaPackageId) {
        DeviceProfileEntity deviceProfileEntity = deviceProfileRepository.findDeviceProfileById(deviceProfileId);
        if (deviceProfileEntity == null) {
            return null;
        }

        if (type.equals(OtaPackageType.FIRMWARE)) {
            deviceProfileEntity.setFirmwareId(otaPackageId);
        }
        if (type.equals(OtaPackageType.SOFTWARE)) {
            deviceProfileEntity.setSoftwareId(otaPackageId);
        }
        if (type.equals(OtaPackageType.CONFIGURE)) {
            deviceProfileEntity.setConfigureId(otaPackageId);
        }
        return DaoUtil.getData(deviceProfileRepository.save(deviceProfileEntity));
    }

    @Override
    public List<DeviceProfile> findByFirmwareId(UUID firmwareId) {
        return DaoUtil.convertDataList(deviceProfileRepository.findByFirmwareId(firmwareId));
    }

    @Override
    public List<DeviceProfile> findBySoftwareId(UUID softwareId) {
        return DaoUtil.convertDataList(deviceProfileRepository.findBySoftwareId(softwareId));
    }

    @Override
    public List<DeviceProfile> findByConfigureId(UUID configureId) {
        return DaoUtil.convertDataList(deviceProfileRepository.findByConfigureId(configureId));
    }

    @Override
    public DeviceProfile findByTenantIdAndExternalId(UUID tenantId, UUID externalId) {
        return DaoUtil.getData(deviceProfileRepository.findByTenantIdAndExternalId(tenantId, externalId));
    }

    @Override
    public DeviceProfile findByTenantIdAndName(UUID tenantId, String name) {
        return DaoUtil.getData(deviceProfileRepository.findByTenantIdAndName(tenantId, name));
    }

    @Override
    public PageData<DeviceProfile> findByTenantId(UUID tenantId, PageLink pageLink) {
        return findDeviceProfiles(TenantId.fromUUID(tenantId), pageLink);
    }

    @Override
    public DeviceProfileId getExternalIdByInternal(DeviceProfileId internalId) {
        return Optional.ofNullable(deviceProfileRepository.getExternalIdById(internalId.getId()))
                .map(DeviceProfileId::new).orElse(null);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DEVICE_PROFILE;
    }

}
