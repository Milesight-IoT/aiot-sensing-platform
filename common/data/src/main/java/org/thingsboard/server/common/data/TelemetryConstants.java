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

import java.util.ArrayList;
import java.util.List;

public class TelemetryConstants {
    public static final String IMAGE = "image";

    public static final List<String> EXCLUDE_TELEMETRY_KEYS = new ArrayList<>();
    static {
        EXCLUDE_TELEMETRY_KEYS.add("fw_state");
        EXCLUDE_TELEMETRY_KEYS.add("target_fw_title");
        EXCLUDE_TELEMETRY_KEYS.add("target_fw_version");
        EXCLUDE_TELEMETRY_KEYS.add("target_fw_tag");
        EXCLUDE_TELEMETRY_KEYS.add("target_fw_ts");

        EXCLUDE_TELEMETRY_KEYS.add("sw_state");
        EXCLUDE_TELEMETRY_KEYS.add("target_sw_title");
        EXCLUDE_TELEMETRY_KEYS.add("target_sw_version");
        EXCLUDE_TELEMETRY_KEYS.add("target_sw_tag");
        EXCLUDE_TELEMETRY_KEYS.add("target_sw_ts");
    }

    public static final String FW_TITLE = "fw_title";
//    public static final String FW_VERSION = "fw_version";
    public static final String FW_CHECKSUM = "fw_checksum";
    public static final List<String> FW_ATTRIBUTE_KEYS = new ArrayList<>();
    static {
        FW_ATTRIBUTE_KEYS.add(FW_TITLE);
//        FW_ATTRIBUTE_KEYS.add(FW_VERSION);
        FW_ATTRIBUTE_KEYS.add(FW_CHECKSUM);
    }

    public static final String SW_TITLE = "sw_title";
//    public static final String SW_VERSION = "sw_version";
    public static final String SW_CHECKSUM = "sw_checksum";
    public static final List<String> SW_ATTRIBUTE_KEYS = new ArrayList<>();
    static {
        SW_ATTRIBUTE_KEYS.add(SW_TITLE);
//        SW_ATTRIBUTE_KEYS.add(SW_VERSION);
        SW_ATTRIBUTE_KEYS.add(SW_CHECKSUM);
    }

    public static final String CF_TITLE = "cf_title";
//    public static final String CF_VERSION = "cf_version";
    public static final String CF_CHECKSUM = "cf_checksum";
    public static final List<String> CF_ATTRIBUTE_KEYS = new ArrayList<>();
    static {
        CF_ATTRIBUTE_KEYS.add(CF_TITLE);
//        CF_ATTRIBUTE_KEYS.add(CF_VERSION);
        CF_ATTRIBUTE_KEYS.add(CF_CHECKSUM);
    }

    public static final String CURRENT_FW_TITLE = "current_fw_title";
    public static final String CURRENT_FW_CHECKSUM = "current_fw_checksum";
    public static final String CURRENT_CF_TITLE = "current_cf_title";
    public static final String CURRENT_CF_CHECKSUM = "current_cf_checksum";
//    public static final String CURRENT_FW_VERSION = "current_fw_version";
//    public static final String CURRENT_SW_TITLE = "current_sw_title";
//    public static final String CURRENT_SW_VERSION = "current_sw_version";
//    public static final String CURRENT_SW_CHECKSUM = "current_sw_checksum";
//    public static final String CURRENT_CF_VERSION = "current_cf_version";
    public static final List<String> CURRENT_DEVICE_OTA_PACKAGE = new ArrayList<>();
    static {
        CURRENT_DEVICE_OTA_PACKAGE.add(CURRENT_FW_TITLE);
        CURRENT_DEVICE_OTA_PACKAGE.add(CURRENT_FW_CHECKSUM);

        CURRENT_DEVICE_OTA_PACKAGE.add(CURRENT_CF_TITLE);
        CURRENT_DEVICE_OTA_PACKAGE.add(CURRENT_CF_CHECKSUM);
    }
}
