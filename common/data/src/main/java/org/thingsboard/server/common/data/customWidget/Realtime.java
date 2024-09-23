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

package org.thingsboard.server.common.data.customWidget;

import lombok.Data;

/**
 * @author zhangzy
 * @Data 2023.04.14
 */
@Data
public class Realtime {

    private int realtimeType;

    private int interval;

    private int timewindowMs;

    private String quickInterval;

    public Realtime() {
        this.realtimeType = 1;
        this.interval = 1000;
        this.timewindowMs = 60000;
        this.quickInterval = "CURRENT_DAY";
    }

}
