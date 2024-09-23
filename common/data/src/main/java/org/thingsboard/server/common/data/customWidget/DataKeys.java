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
import lombok.NoArgsConstructor;

/**
 * Auto-generated: 2023-04-14 13:31:41
 *
 * @author zhangzy
 */
@Data
@NoArgsConstructor
public class DataKeys {

    private String name;
    private String type;
    private String label;
    private String color;
    private Settings settings;
    private double _hash;
    private String aggregationType;
    private String units;
    private String decimals;
    private String funcBody;
    private String usePostProcessing;
    private String postFuncBody;

    public DataKeys(String name, String type, String label, String color, double _hash) {
        this.name = name;
        this.type = type;
        this.label = label;
        this.color = color;
        this.settings = new Settings();
        this._hash = _hash;
        this.aggregationType = null;
        this.units = null;
        this.decimals = null;
        this.funcBody = null;
        this.usePostProcessing = null;
        this.postFuncBody = null;
    }

}