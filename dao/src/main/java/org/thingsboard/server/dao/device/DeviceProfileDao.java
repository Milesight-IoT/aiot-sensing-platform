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
package org.thingsboard.server.dao.device;

import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.DeviceProfileInfo;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.ExportableEntityDao;

import java.util.List;
import java.util.UUID;

public interface DeviceProfileDao extends Dao<DeviceProfile>, ExportableEntityDao<DeviceProfileId, DeviceProfile> {

    /**
     * 查找租户和系统设备型号
     *
     * @param tenantId 住户ID
     * @param pageLink 分页信息
     * @return {@link DeviceProfile}
     */
    PageData<DeviceProfile> findDeviceProfiles(TenantId tenantId, PageLink pageLink);

    /**
     * 查找需要删除的设备型号
     *
     * @param tenantId    住户ID
     * @param pageLink    分页信息
     * @param profileType 设备型号类型,默认0,新增字段 0 - 自定义型号（当前租户可见）1 - 系统型号（所有租户共享）
     * @return {@link DeviceProfile}
     */
    PageData<DeviceProfile> findDeleteDeviceProfiles(TenantId tenantId, PageLink pageLink, Short profileType);

    DeviceProfileInfo findDeviceProfileInfoById(TenantId tenantId, UUID deviceProfileId);

    DeviceProfile save(TenantId tenantId, DeviceProfile deviceProfile);

    DeviceProfile saveAndFlush(TenantId tenantId, DeviceProfile deviceProfile);

    PageData<DeviceProfileInfo> findDeviceProfileInfos(TenantId tenantId, PageLink pageLink, String transportType);

    DeviceProfile findDefaultDeviceProfile(TenantId tenantId);

    DeviceProfileInfo findDefaultDeviceProfileInfo(TenantId tenantId);

    DeviceProfile findByProvisionDeviceKey(String provisionDeviceKey);

    DeviceProfile findByName(TenantId tenantId, String profileName);

    DeviceProfile updateDeviceProfileOtaId(UUID deviceProfileId, OtaPackageType type, UUID otaPackageId);

    List<DeviceProfile> findByFirmwareId(UUID id);

    List<DeviceProfile> findBySoftwareId(UUID id);

    List<DeviceProfile> findByConfigureId(UUID id);
}
