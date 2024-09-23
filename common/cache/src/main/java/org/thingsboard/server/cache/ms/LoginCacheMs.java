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

package org.thingsboard.server.cache.ms;

import org.springframework.stereotype.Component;

/**
 * 设备遥测识别部署 缓存
 *
 * @author Luohh
 */
@Component
public class LoginCacheMs extends MsAbstractCache<String, String> {
    private static final int MAX_SIZE = 1000;

    public LoginCacheMs() {
        super(MAX_SIZE);
    }

    public void put(String key, String value) {
        // 两小时过期
        put(key, value, 60 * 60 * 2);
    }

}
