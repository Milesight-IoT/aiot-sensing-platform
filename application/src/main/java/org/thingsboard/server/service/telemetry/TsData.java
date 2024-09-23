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
package org.thingsboard.server.service.telemetry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class TsData implements Comparable<TsData>{

    private final long ts;
    private final Object value;

    public TsData(long ts, Object value) {
        super();
        this.ts = ts;
        this.value = value;
    }

    @ApiModelProperty(position = 1, value = "Timestamp last updated timeseries, in milliseconds", example = "1609459200000", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    public long getTs() {
        return ts;
    }

    @ApiModelProperty(position = 2, value = "Object representing value of timeseries key", example = "20", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    public Object getValue() {
        return value;
    }

    @Override
    public int compareTo(TsData o) {
        return Long.compare(ts, o.ts);
    }

}
