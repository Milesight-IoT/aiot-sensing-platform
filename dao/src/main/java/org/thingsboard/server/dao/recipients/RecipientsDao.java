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

package org.thingsboard.server.dao.recipients;

import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.dao.Dao;

import java.util.List;
import java.util.UUID;

/**
 * 接收方网络配置管理 - Dao
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:09
 */
public interface RecipientsDao extends Dao<Recipients> {
    /**
     * 查找接收方管理列表
     *
     * @param tenantId 租户ID
     * @param pageLink 分页条件
     * @return {@link Recipients}
     */
    PageData<Recipients> findRecipientsList(UUID tenantId, PageLink pageLink);

    @Override
    default EntityType getEntityType() {
        return EntityType.RECIPIENTS;
    }

    /**
     * （MS）根据名称获取详情
     *
     * @param tenantId 租户ID
     * @param name     名称
     * @return {@link Recipients}
     */
    Recipients getByTenantIdAndName(UUID tenantId, String name);

    /**
     * 批量查找（ID）
     *
     * @param uuidList 主键ID
     * @return {@link Recipients}
     */
    List<Recipients> findByIds(List<UUID> uuidList);
}
