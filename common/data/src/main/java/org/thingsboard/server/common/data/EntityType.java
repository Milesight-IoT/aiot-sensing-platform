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
package org.thingsboard.server.common.data;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrew Shvayka
 */
public enum EntityType {
    RULE_CHAIN_OWN, RECIPIENTS ,DASHBOARD_RULE_DEVICES,
    // OLD
    TENANT,
    CUSTOMER,
    USER,
    DASHBOARD,
    ASSET,
    DEVICE,
    ALARM,
    RULE_CHAIN,
    RULE_NODE,
    ENTITY_VIEW,
    WIDGETS_BUNDLE,
    WIDGET_TYPE,
    TENANT_PROFILE,
    DEVICE_PROFILE,
    ASSET_PROFILE,
    API_USAGE_STATE,
    TB_RESOURCE,
    OTA_PACKAGE,
    EDGE,
    RPC,
    QUEUE,
    NOTIFICATION_TARGET,
    NOTIFICATION_TEMPLATE,
    NOTIFICATION_REQUEST,
    NOTIFICATION,
    NOTIFICATION_RULE;

    @Getter
    private final String normalName = StringUtils.capitalize(StringUtils.removeStart(name(), "TB_")
            .toLowerCase().replaceAll("_", " "));

}
