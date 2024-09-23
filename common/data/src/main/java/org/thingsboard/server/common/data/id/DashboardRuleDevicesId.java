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

package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

/**
 * @author zhangzy
 * @data 2023-04-19
 */
@ApiModel
public class DashboardRuleDevicesId extends UUIDBased implements EntityId{

    @JsonCreator
    public DashboardRuleDevicesId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static DashboardRuleDevicesId fromString(String dashboardRuleDevicesId) {
        return new DashboardRuleDevicesId(UUID.fromString(dashboardRuleDevicesId));
    }

    @ApiModelProperty(position = 2, required = true, value = "string", example = "DASHBOARD_RULE_DEVICES", allowableValues = "DASHBOARD_RULE_DEVICES")
    @Override
    public EntityType getEntityType() {
        return EntityType.DASHBOARD_RULE_DEVICES;
    }
}
