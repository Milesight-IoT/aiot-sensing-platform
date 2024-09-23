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

import java.util.List;

/**
 * @author zhangzy
 * 部件数据源
 * @Data 2023.04.14
 */
@Data
@NoArgsConstructor
public class Datasources {
    private String type;
    private String name;
    private String entityAliasId;
    private String filterId;
    private List<DataKeys> dataKeys;

    public Datasources(String type, String entityAliasId, String filterId, List<DataKeys> dataKeys) {
        this.type = type;
        this.name = null;
        this.entityAliasId = entityAliasId;
        this.filterId = filterId;
        this.dataKeys = dataKeys;
    }
}
