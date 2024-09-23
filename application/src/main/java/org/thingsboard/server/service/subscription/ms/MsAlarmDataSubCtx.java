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
import org.springframework.util.CollectionUtils;
import org.thingsboard.server.common.data.DashboardRuleDevices;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.EntityCountQuery;
import org.thingsboard.server.common.data.query.ms.MsAlarmDataQuery;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.dashboard.DashboardWebSocketService;
import org.thingsboard.server.dao.entity.EntityService;
import org.thingsboard.server.service.subscription.SubscriptionServiceStatistics;
import org.thingsboard.server.service.subscription.TbAbstractSubCtx;
import org.thingsboard.server.service.subscription.TbLocalSubscriptionService;
import org.thingsboard.server.service.ws.WebSocketService;
import org.thingsboard.server.service.ws.WebSocketSessionRef;
import org.thingsboard.server.service.ws.telemetry.ms.MsAlarmDataUpdate;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Luohh
 */
@Slf4j
public class MsAlarmDataSubCtx extends TbAbstractSubCtx<EntityCountQuery> {

    private final DashboardWebSocketService service;
    private final Long startTime;

    public MsAlarmDataSubCtx(DashboardWebSocketService service,
                             String serviceId, WebSocketService wsService, EntityService entityService,
                             TbLocalSubscriptionService localSubscriptionService, AttributesService attributesService,
                             SubscriptionServiceStatistics stats, WebSocketSessionRef sessionRef, int cmdId) {
        super(serviceId, wsService, entityService, localSubscriptionService, attributesService, stats, sessionRef, cmdId);
        this.service = service;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void fetchData() {
        MsAlarmDataQuery dataQuery = (MsAlarmDataQuery) query;
        Long time = dataQuery.getStartTime();
        if (Objects.isNull(time)) {
            dataQuery.setStartTime(this.startTime);
        }
        log.debug("send ws ms alarm data message fetch_data! cmdId = {},sessionRef = {}", cmdId, sessionRef);
        Optional<PageData<DashboardRuleDevices>> optional = service.sendWsMsAlarmDataMessage(dataQuery, getTenantId());
        if (optional.isPresent() && !CollectionUtils.isEmpty(optional.get().getData())) {
            sendWsMsg(new MsAlarmDataUpdate(cmdId, optional.get()));
        }
    }


    @Override
    protected void update() {
        MsAlarmDataQuery dataQuery = (MsAlarmDataQuery) query;
        Optional<PageData<DashboardRuleDevices>> optional = service.sendWsMsAlarmDataMessage(dataQuery, getTenantId());
        if (optional.isPresent() && !CollectionUtils.isEmpty(optional.get().getData())) {
            sendWsMsg(new MsAlarmDataUpdate(cmdId, optional.get()));
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

}
