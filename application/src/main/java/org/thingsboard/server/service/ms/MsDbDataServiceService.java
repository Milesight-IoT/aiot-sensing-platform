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

package org.thingsboard.server.service.ms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.sensing.SensingObjectService;
import org.thingsboard.server.dao.tenant.TenantService;

/**
 * （MS）重启-系统预热
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/6/25 11:37
 */
@Slf4j
@Service
public class MsDbDataServiceService {
    protected static final int DEFAULT_PAGE_SIZE = 100;

    @Autowired
    private SensingObjectService sensingObjectService;
    @Autowired
    private TenantService tenantService;

    public void initMsDbData() {
        PageLink pageLink = new PageLink(DEFAULT_PAGE_SIZE);
        PageData<TenantId> tenantsIds;
        do {
            tenantsIds = tenantService.findTenantsIds(pageLink);
            for (TenantId tenantId : tenantsIds.getData()) {
                // 缓存感知通道
                sensingObjectService.findSelectListByTenantId(tenantId, new PageLink(10000), true, null, false);
            }
            pageLink = pageLink.nextPageLink();
        } while (tenantsIds.hasNext());
    }


}
