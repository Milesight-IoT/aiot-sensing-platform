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
package org.thingsboard.server.common.data.lwm2m;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.stream.Stream;

@ApiModel
@Data
@AllArgsConstructor
public class LwM2mResourceObserve {
    @ApiModelProperty(position = 1, value = "LwM2M Resource Observe id.", example = "0")
    int id;
    @ApiModelProperty(position = 2, value = "LwM2M Resource Observe name.", example = "Data")
    String name;
    @ApiModelProperty(position = 3, value = "LwM2M Resource Observe observe.", example = "false")
    boolean observe;
    @ApiModelProperty(position = 4, value = "LwM2M Resource Observe attribute.", example = "false")
    boolean attribute;
    @ApiModelProperty(position = 5, value = "LwM2M Resource Observe telemetry.", example = "false")
    boolean telemetry;
    @ApiModelProperty(position = 6, value = "LwM2M Resource Observe key name.", example = "data")
    String keyName;

    public LwM2mResourceObserve(int id, String name, boolean observe, boolean attribute, boolean telemetry) {
        this.id = id;
        this.name = name;
        this.observe = observe;
        this.attribute = attribute;
        this.telemetry = telemetry;
        this.keyName = getCamelCase (this.name);
    }

    private String getCamelCase (String name) {
        name = name.replaceAll("-", " ");
        name = name.replaceAll("_", " ");
        String [] nameCamel1 = name.split(" ");
        String [] nameCamel2 = new String[nameCamel1.length];
        int[] idx = { 0 };
        Stream.of(nameCamel1).forEach((s -> {
            nameCamel2[idx[0]] = toProperCase(idx[0]++,  s);
        }));
        return String.join("", nameCamel2);
    }

    private String toProperCase(int idx, String s) {
        if (!s.isEmpty() && s.length()> 0) {
            String s1 = (idx == 0) ? s.substring(0, 1).toLowerCase() : s.substring(0, 1).toUpperCase();
            String s2 = "";
            if (s.length()> 1) s2 = s.substring(1).toLowerCase();
            s = s1 + s2;
        }
        return s;
    }
}
