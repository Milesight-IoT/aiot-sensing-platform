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

package org.thingsboard.server.dao.dashboard;

import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.ms.MsAlarmDataQuery;

import java.util.Optional;

/**
 * Description:
 * Version 1.0
 *
 * @author zhangzy
 * @date 2023/4/20 19:53
 */
public interface DashboardWebSocketService {

    /**
     * 发送列表仪表板消息
     *
     * @param query    查找数据
     * @param tenantId 租户ID
     * @return {@link DashboardRuleDevices}
     */
    Optional<PageData<DashboardRuleDevices>> sendWsMsAlarmDataMessage(MsAlarmDataQuery query, TenantId tenantId);

    /**
     * 发送SnapshotPreview仪表板消息
     *
     * @param isGet    是否强制获取
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @return {@link TsKvEntry}
     */
    Optional<TsKvEntry> sendWsSnapshotPreviewMessage(boolean isGet, TenantId tenantId, DeviceId deviceId);

    /**
     * 获取能力
     *
     * @param abilityId 能力ID
     * @return {@link DeviceAbility}
     */
    Optional<DeviceAbility> getRoiAbility(String abilityId);

    /**
     * 获取感知通道
     *
     * @param sensingObjectId 感知通道ID
     * @return {@link SensingObject}
     */
    Optional<SensingObject> getSensingObjectById(String sensingObjectId);
}
