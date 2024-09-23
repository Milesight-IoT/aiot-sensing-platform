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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 设备遥测图片保存最后时间 缓存
 *
 * @author Luohh
 */
@Component
public class MsDeviceLastUpdateCache extends MsAbstractCache<String, Integer> {
    private static final int MAX_SIZE = 1500;
    @Value("${server.ws.dynamic_page_link.refresh_interval:60}")
    private long dynamicPageLinkRefreshInterval;

    public MsDeviceLastUpdateCache() {
        super(MAX_SIZE);
    }

    public void put(String key) {
        // 2分钟过期
        put(key, 1, dynamicPageLinkRefreshInterval * 2);
    }

}
