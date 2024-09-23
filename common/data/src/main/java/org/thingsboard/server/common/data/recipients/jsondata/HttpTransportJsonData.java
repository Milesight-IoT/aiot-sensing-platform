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

package org.thingsboard.server.common.data.recipients.jsondata;

import lombok.Data;

import java.io.Serializable;

/**
 * Http类型属性 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 13:29
 */
@Data
public class HttpTransportJsonData implements Serializable {

    /**
     * 地址
     */
    private String url;
}
