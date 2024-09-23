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

package org.thingsboard.server.dao.sql.recipients;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.RecipientsEntity;
import org.thingsboard.server.dao.recipients.RecipientsDao;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

;

/**
 * 接收方网络配置管理 - JpaDao
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:08
 */
@Component
@Slf4j
public class JpaRecipientsDao extends JpaAbstractSearchTextDao<RecipientsEntity, Recipients> implements RecipientsDao {
    @Resource
    private RecipientsRepository recipientsRepository;

    @Override
    protected Class<RecipientsEntity> getEntityClass() {
        return RecipientsEntity.class;
    }

    @Override
    protected JpaRepository<RecipientsEntity, UUID> getRepository() {
        return recipientsRepository;
    }

    @Override
    public PageData<Recipients> findRecipientsList(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(
                recipientsRepository.findRecipientsList(tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink))
        );
    }

    @Override
    public Recipients getByTenantIdAndName(UUID tenantId, String name) {
        List<Recipients> list = DaoUtil.convertDataList(recipientsRepository.findByTenantIdAndName(tenantId, name));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public List<Recipients> findByIds(List<UUID> uuidList) {
        return DaoUtil.convertDataList(recipientsRepository.findAllById(uuidList));
    }
}