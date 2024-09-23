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
package org.thingsboard.server.service.ws.telemetry.ms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.service.subscription.SubscriptionErrorCode;
import org.thingsboard.server.service.ws.telemetry.cmd.v2.CmdUpdate;
import org.thingsboard.server.service.ws.telemetry.cmd.v2.CmdUpdateType;

/**
 * 星纵 - websocket Snapshot preview CmdUpdate
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/6/19 8:55
 */
@ToString
public class MsSnapshotPreviewUpdate extends CmdUpdate {

    @Getter
    private TsKvEntry tsKvEntry;
    @Getter
    private DeviceAbility deviceAbility;
    @Getter
    private SensingObject sensingObject;

    public MsSnapshotPreviewUpdate(int cmdId, TsKvEntry tsKvEntry, DeviceAbility deviceAbility, SensingObject sensingObject) {
        super(cmdId, SubscriptionErrorCode.NO_ERROR.getCode(), null);
        this.tsKvEntry = tsKvEntry;
        this.deviceAbility = deviceAbility;
        this.sensingObject = sensingObject;
    }

    public MsSnapshotPreviewUpdate(int cmdId, int errorCode, String errorMsg) {
        super(cmdId, errorCode, errorMsg);
    }

    @Override
    public CmdUpdateType getCmdUpdateType() {
        return CmdUpdateType.ENTITY_DATA;
    }

    @JsonCreator
    public MsSnapshotPreviewUpdate(@JsonProperty("cmdId") int cmdId,
                                   @JsonProperty("tsKvEntry") TsKvEntry tsKvEntry,
                                   @JsonProperty("deviceAbility") DeviceAbility deviceAbility,
                                   @JsonProperty("SensingObject") SensingObject sensingObject,
                                   @JsonProperty("errorCode") int errorCode,
                                   @JsonProperty("errorMsg") String errorMsg) {
        super(cmdId, errorCode, errorMsg);
        this.tsKvEntry = tsKvEntry;
        this.deviceAbility = deviceAbility;
        this.sensingObject = sensingObject;
    }

}
