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
import org.thingsboard.server.common.data.query.ms.MsAlarmDataQuery;
import org.thingsboard.server.service.ws.telemetry.cmd.v2.DataCmd;

/**
 * 星纵 - websocket Alarm Table 查询命令
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/6/19 8:55
 */
public class MsAlarmDataCmd extends DataCmd {

    @Getter
    private final MsAlarmDataQuery query;

    @JsonCreator
    public MsAlarmDataCmd(@JsonProperty("cmdId") int cmdId, @JsonProperty("query") MsAlarmDataQuery query) {
        super(cmdId);
        this.query = query;
    }
}