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
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * （MS）设备能力服务
 *
 * @author Luohh
 * @date 2023年6月5日16:26:46
 */
public interface DeviceAbilityService {
    /**
     * 分页查询
     */
    PageData<DeviceAbility> findDeviceAbilitiesByDeviceIdAndAbilityType(UUID deviceId, short abilityType, PageLink pageLink);

    /**
     * 保存能力
     *
     * @param deviceAbility 保存能力
     * @return 能力
     */
    DeviceAbility saveDeviceAbilityAndCache(DeviceAbility deviceAbility);

    /**
     * 删除能力
     *
     * @param deviceAbilityId 能力ID
     */
    void deleteDeviceAbility(UUID deviceAbilityId);

    /**
     * 通过设备ID删除
     *
     * @param deviceId 设备ID
     */
    void deleteByDeviceId(UUID deviceId);

    /**
     * 根据设备ID和能力值获取设备能力
     *
     * @param deviceId 设备ID
     * @param ability  能力
     * @return {@link DeviceAbility}
     */
    DeviceAbility findDeviceAbilityByDeviceIdAndAbility(UUID deviceId, String ability);

    /**
     * 批量获取能力对象（缓存里面取）
     *
     * @param abilityIdsByIds 主键数组
     * @return {@link DeviceAbility }
     */
    List<DeviceAbility> findCacheByIds(List<String> abilityIdsByIds);

    /**
     * 修改能力
     *
     * @param sensingObjectId 感知通道ID
     */
    void updateSensingObjectIdBySensingObjectId(UUID sensingObjectId);

    /**
     * 批量保存
     *
     * @param deviceAbilities 能力
     */
    void saveAll(List<DeviceAbility> deviceAbilities);

    /**
     * 获取详情（缓存）
     *
     * @param abilityId 能力ID
     * @return {@link DeviceAbility}
     */
    DeviceAbility getDeviceAbilityByIdCache(UUID abilityId);

    /**
     * (异步)批量保存
     *
     * @param deviceAbilities 能力
     */
    void saveAllSync(List<DeviceAbility> deviceAbilities);

    /**
     * 批量查找
     *
     * @param uuids id集合
     * @return {@link DeviceAbility}
     */
    List<DeviceAbility> findAllById(List<UUID> uuids);

    /**
     * 批量查找
     *
     * @param uuids id集合
     * @return {@link DeviceAbilityInfo}
     */
    List<DeviceAbilityInfo> findByIdIn(ArrayList<UUID> uuids);

    /**
     * 获取设备能力信息
     *
     * @param uuid 主键id
     * @return {@link DeviceAbilityInfo}
     */
    DeviceAbilityInfo getDeviceAbilityInfoById(UUID uuid);

    /**
     * 获取设备能力
     *
     * @param deviceId 设备ID
     * @return {@link DeviceAbility}
     */
    List<DeviceAbility> findByDeviceId(UUID deviceId);

    /**
     * 获取ROI下拉列表
     *
     * @param tenantId    租户ID
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param pageLink    分页信息pageLink
     * @param isRoi       是否只需要ROI
     * @return {@link RoiSelectData}
     */
    PageData<RoiSelectData> findRoiSelectList(TenantId tenantId, Short abilityType, PageLink pageLink, boolean isRoi);

    /**
     * 获取设备-ROI下拉详情列表
     *
     * @param tenantId    租户ID
     * @param deviceIds   设备ID
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param isRoi       是否只需要ROI
     * @return {@link RoiSelectData}
     */
    List<RoiSelectData> findRoiSelectByDeviceList(TenantId tenantId, String deviceIds, Short abilityType, boolean isRoi) throws ThingsboardException;
}
