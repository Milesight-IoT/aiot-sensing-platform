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
package org.thingsboard.server.dao.sensing;

import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.customWidget.SensingObjectImage;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface SensingObjectService {
    PageData<SensingObject> findByTenantId(TenantId tenantId, PageLink pageLink) throws ExecutionException, InterruptedException;

    TsKvEntry getTsKvEnTry(TenantId tenantId, DeviceId deviceId);

    TsKvEntry findTsKvEnTry(TenantId tenantId, DeviceId deviceId) throws ExecutionException, InterruptedException;

    SensingObject saveSensingObject(TenantId tenantId, SensingObject sensingObject) throws Exception;

    void deleteSensingObject(UUID sensingObjectId) throws ThingsboardException;

    List<SensingObject> findByDeviceId(UUID deviceId);

    SensingObject findByDeviceAbilityId(UUID deviceAbilityId);

    SensingObject findByTenantIdAndName(TenantId tenantId, String name);

    /**
     * 批量获取感知对象
     *
     * @param sensingObjectId 感知对象ID数组
     * @return {@link SensingObject }
     */
    List<SensingObject> findCacheByIds(List<String> sensingObjectId);

    /**
     * 获取详情（缓存）
     *
     * @param uuid 主键ID
     * @return {@link SensingObject }
     */
    SensingObject getSensingObjectById(UUID uuid);

    /**
     * 批量获取能力ID
     *
     * @param sensingObjectId 感知对象ID数组
     * @return {@link String }
     */
    List<String> findAbilityIdsBySensingObjectIds(List<String> sensingObjectId);

    /**
     * 查找图片感知列表（下拉选择框）
     *
     * @param tenantId 租户
     * @param pageLink 分页信息
     * @param isRoi
     * @return {@link SensingObjectImage}
     */
    PageData<SensingObject> findSensingObjectImage(TenantId tenantId, PageLink pageLink, boolean isRoi);

    /**
     * （MS）获取感知对象-下拉列表
     *
     * @param tenantId     租户id
     * @param pageLink     分页信息
     * @param needChannels 是否需要sensingChannels数据
     * @param abilityType  能力类型,1.普通 2.图片
     * @param isRoi
     * @return {@link SensingObject}
     */
    PageData<SensingObject> findSelectListByTenantId(TenantId tenantId, PageLink pageLink, boolean needChannels, String abilityType, boolean isRoi);

    /**
     * （MS）ID列表获取感知通道数据
     *
     * @param tenantId          组合ID
     * @param sensingObjectsIds 感知ID,多个逗号隔开
     * @param needChannels      是否需要sensingChannels数据
     * @param abilityType       能力类型,1.普通 2.图片
     * @param isRoi
     * @return {@link SensingObject}
     */
    List<SensingObject> findSensingObjectsByIds(TenantId tenantId, String sensingObjectsIds, boolean needChannels, String abilityType, boolean isRoi);

}
