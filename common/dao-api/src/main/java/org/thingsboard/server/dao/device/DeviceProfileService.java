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
import org.thingsboard.server.dao.entity.EntityDaoService;

import java.util.List;
import java.util.UUID;

public interface DeviceProfileService extends EntityDaoService {

    DeviceProfile findDeviceProfileById(TenantId tenantId, DeviceProfileId deviceProfileId);

    DeviceProfile findDeviceProfileByName(TenantId tenantId, String profileName);

    DeviceProfileInfo findDeviceProfileInfoById(TenantId tenantId, DeviceProfileId deviceProfileId);

    DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile);

    void deleteDeviceProfile(TenantId tenantId, DeviceProfileId deviceProfileId);

    PageData<DeviceProfile> findDeviceProfiles(TenantId tenantId, PageLink pageLink);

    PageData<DeviceProfileInfo> findDeviceProfileInfos(TenantId tenantId, PageLink pageLink, String transportType);

    DeviceProfile findDeviceProfileByProvisionDeviceKey(String provisionDeviceKey);

    DeviceProfile findOrCreateDeviceProfile(TenantId tenantId, String profileName);

    DeviceProfile createDefaultDeviceProfile(TenantId tenantId);

    DeviceProfile findDefaultDeviceProfile(TenantId tenantId);

    DeviceProfileInfo findDefaultDeviceProfileInfo(TenantId tenantId);

    boolean setDefaultDeviceProfile(TenantId tenantId, DeviceProfileId deviceProfileId);

    void deleteDeviceProfilesByTenantId(TenantId tenantId);

    DeviceProfile updateDeviceProfileOtaId(UUID deviceProfileId, OtaPackageType type, UUID otaPackageId);

    List<DeviceProfile> findByOtaPackageId(UUID otaPackageId, OtaPackageType type);
}
