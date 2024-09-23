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
package org.thingsboard.server.dao.sql.attributes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.AttributeRecognition;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.attributes.AttributeRecognitionDao;
import org.thingsboard.server.dao.model.sql.AttributeRecognitionEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;

import java.util.UUID;

@Component
@Slf4j
public class JpaAttributeRecognitionDao extends JpaAbstractDao<AttributeRecognitionEntity, AttributeRecognition> implements AttributeRecognitionDao {
    @Autowired
    private AttributeRecognitionRepository attributeRecognitionRepository;

    @Override
    public PageData<AttributeRecognition> findAttributeRecognition(UUID tenantId, PageLink pageLink) {
        Page<AttributeRecognitionEntity> attributeRecognition = attributeRecognitionRepository.findAttributeRecognition(tenantId, DaoUtil.toPageable(pageLink));
        return DaoUtil.toPageData(attributeRecognition);
    }

    @Override
    protected Class<AttributeRecognitionEntity> getEntityClass() {
        return AttributeRecognitionEntity.class;
    }

    @Override
    protected JpaRepository<AttributeRecognitionEntity, UUID> getRepository() {
        return attributeRecognitionRepository;
    }
}
