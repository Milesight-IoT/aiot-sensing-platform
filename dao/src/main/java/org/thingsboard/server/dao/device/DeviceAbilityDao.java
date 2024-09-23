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

import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.DeviceAbilityInfo;
import org.thingsboard.server.common.data.device.RoiSelectData;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.Dao;

import java.util.List;
import java.util.UUID;

/**
 * The Interface DeviceAbilityDao.
 */
public interface DeviceAbilityDao extends Dao<DeviceAbility> {

    PageData<DeviceAbility> findDeviceAbilitiesByDeviceId(UUID deviceId, PageLink pageLink);

    PageData<DeviceAbility> findDeviceAbilitiesByDeviceIdAndAbilityType(UUID deviceId, short abilityType, PageLink pageLink);

    List<DeviceAbility> findByDeviceId(UUID deviceId);

    int updateSensingObjectIdBySensingObjectId(UUID sensingObjectId);

    List<DeviceAbilityInfo> findByIdIn(List<UUID> deviceAbilityIdList);

    /**
     * 获取设备能力对象数组
     *
     * @param deviceAbilityIdList 设备能力ID数组
     * @return {@link List<DeviceAbilityInfo>}
     */
    List<DeviceAbility> findAllById(List<UUID> deviceAbilityIdList);

    DeviceAbilityInfo findDeviceAbilityInfoById(UUID deviceAbilityId);

    DeviceAbility findDeviceAbilityByDeviceIdAndAbility(UUID deviceId, String ability);

    List<DeviceAbility> findAllBySensingObjectId(UUID sensingObjectId);

    /**
     * 获取设备ROI通道
     *
     * @param deviceIdList 设备ID
     * @return {@link List<DeviceAbility>}
     */
    List<DeviceAbility> findImageByDeviceIds(List<DeviceId> deviceIdList);

    /**
     * 获取ROI下拉列表
     *
     * @param tenantId    租户ID
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param pageLink    分页信息pageLink
     * @param isRoi       是否只需要ROI
     * @return {@link RoiSelectData}
     */
    PageData<RoiSelectData> findRoiSelectList(UUID tenantId, PageLink pageLink, boolean isRoi, Short abilityType);

    /**
     * 获取设备-ROI下拉详情列表
     *
     * @param tenantId     租户ID
     * @param deviceIdList 设备ID
     * @param isRoi        是否只需要ROI
     * @param abilityType  能力类型 0-所有; 1-基础能力; 2-图片能力
     * @return {@link RoiSelectData}
     */
    List<RoiSelectData> findRoiSelectByDeviceList(UUID tenantId, List<UUID> deviceIdList, boolean isRoi, Short abilityType);
}
