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
package org.thingsboard.server.service.subscription.ms;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.query.EntityCountQuery;
import org.thingsboard.server.common.data.query.ms.MsSnapshotPreviewQuery;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.dashboard.DashboardWebSocketService;
import org.thingsboard.server.dao.entity.EntityService;
import org.thingsboard.server.service.subscription.SubscriptionErrorCode;
import org.thingsboard.server.service.subscription.SubscriptionServiceStatistics;
import org.thingsboard.server.service.subscription.TbAbstractSubCtx;
import org.thingsboard.server.service.subscription.TbLocalSubscriptionService;
import org.thingsboard.server.service.ws.WebSocketService;
import org.thingsboard.server.service.ws.WebSocketSessionRef;
import org.thingsboard.server.service.ws.telemetry.ms.MsSnapshotPreviewUpdate;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Luohh
 */
@Slf4j
public class MsSnapshotPreviewSubCtx extends TbAbstractSubCtx<EntityCountQuery> {

    private final DashboardWebSocketService dashboardWebSocketService;
    private int errorTimes;
    private static final int ERROR_TIMES_LIMIT = 3;

    public MsSnapshotPreviewSubCtx(DashboardWebSocketService dashboardWebSocketService,
                                   String serviceId, WebSocketService wsService, EntityService entityService,
                                   TbLocalSubscriptionService localSubscriptionService, AttributesService attributesService,
                                   SubscriptionServiceStatistics stats, WebSocketSessionRef sessionRef, int cmdId) {
        super(serviceId, wsService, entityService, localSubscriptionService, attributesService, stats, sessionRef, cmdId);
        this.dashboardWebSocketService = dashboardWebSocketService;
        this.errorTimes = 0;
    }

    @Override
    public void fetchData() {
        getData(true);
    }


    @Override
    protected void update() {
        getData(false);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private void getData(boolean isGet) {
        if (errorTimes == ERROR_TIMES_LIMIT) {
            return;
        }
        MsSnapshotPreviewQuery previewQuery = (MsSnapshotPreviewQuery) query;
        String sensingObjectId = previewQuery.getSensingObjectId();
        Optional<SensingObject> sensingObjectById = dashboardWebSocketService.getSensingObjectById(sensingObjectId);
        if (sensingObjectById.isEmpty()) {
            sendWsMsg(new MsSnapshotPreviewUpdate(cmdId, SubscriptionErrorCode.NO_ERROR.getCode(), "ROI is not exist!"));
            this.errorTimes++;
            return;
        }
        DeviceId deviceId = new DeviceId(UUID.fromString(previewQuery.getDeviceId()));
        Optional<DeviceAbility> ability = dashboardWebSocketService.getRoiAbility(previewQuery.getAbilityId());
        if (ability.isEmpty()) {
            sendWsMsg(new MsSnapshotPreviewUpdate(cmdId, SubscriptionErrorCode.NO_ERROR.getCode(), "ROI is not exist!"));
            this.errorTimes++;
            return;
        }
        this.errorTimes = 0;
        Optional<TsKvEntry> tsKvEntry = dashboardWebSocketService.sendWsSnapshotPreviewMessage(isGet, getTenantId(), deviceId);
        tsKvEntry.ifPresent(kvEntry -> sendWsMsg(new MsSnapshotPreviewUpdate(cmdId, kvEntry, ability.get(), sensingObjectById.get())));
    }
}
