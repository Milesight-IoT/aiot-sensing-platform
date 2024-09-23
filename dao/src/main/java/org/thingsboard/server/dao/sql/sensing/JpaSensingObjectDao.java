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
package org.thingsboard.server.dao.sql.sensing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.SensingObjectEntity;
import org.thingsboard.server.dao.sensing.SensingObjectDao;
import org.thingsboard.server.dao.sql.JpaAbstractDao;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class JpaSensingObjectDao extends JpaAbstractDao<SensingObjectEntity, SensingObject> implements SensingObjectDao {
    @Autowired
    private SensingObjectRepository sensingObjectRepository;

    @Override
    protected Class<SensingObjectEntity> getEntityClass() {
        return SensingObjectEntity.class;
    }

    @Override
    protected JpaRepository<SensingObjectEntity, UUID> getRepository() {
        return sensingObjectRepository;
    }

    @Override
    public PageData<SensingObject> findByTenantId(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(
                sensingObjectRepository.findByTenantId(
                        tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public List<SensingObject> findByDeviceId(UUID deviceId) {
        return DaoUtil.convertDataList(sensingObjectRepository.findByDeviceId(deviceId));
    }

    @Override
    public PageData<SensingObject> findBySensingImageAbility(UUID tenantId, PageLink pageLink, boolean isRoi) {
        return DaoUtil.toPageData(
                sensingObjectRepository.findByDeviceAbilityIdsImage(
                        tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        isRoi,
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public SensingObject findByDeviceAbilityId(UUID deviceAbilityId) {
        return DaoUtil.getData(sensingObjectRepository.findByDeviceAbilityId(deviceAbilityId));
    }

    @Override
    public SensingObject findByTenantIdAndName(UUID tenantId, String name) {
        return DaoUtil.getData(sensingObjectRepository.findByTenantIdAndName(tenantId, name));
    }

    @Override
    public List<SensingObject> findByIds(List<UUID> uuidList) {
        return DaoUtil.convertDataList(sensingObjectRepository.findByIdIn(uuidList));
    }
}
