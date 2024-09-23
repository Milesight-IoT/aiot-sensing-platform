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
package org.thingsboard.server.dao.sql;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.BaseEntity;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Valerii Sosliuk
 */
@Slf4j
@SqlDao
public abstract class JpaAbstractDao<E extends BaseEntity<D>, D>
        extends JpaAbstractDaoListeningExecutorService
        implements Dao<D> {

    protected abstract Class<E> getEntityClass();

    protected abstract JpaRepository<E, UUID> getRepository();

    protected void setSearchText(E entity) {
    }

    @Override
    @Transactional
    public D save(TenantId tenantId, D domain) {
        E entity = getEntity(domain);
        entity = getRepository().save(entity);
        return DaoUtil.getData(entity);
    }

    @NotNull
    private E getEntity(D domain) {
        E entity;
        try {
            entity = getEntityClass().getConstructor(domain.getClass()).newInstance(domain);
        } catch (Exception e) {
            log.error("Can't create entity for domain object {}", domain, e);
            throw new IllegalArgumentException("Can't create entity for domain object {" + domain + "}", e);
        }
        setSearchText(entity);
        log.debug("Saving entity {}", entity);
        if (entity.getUuid() == null) {
            UUID uuid = Uuids.timeBased();
            entity.setUuid(uuid);
            if (entity.getCreatedTime() == 0) {
                entity.setCreatedTime(Uuids.unixTimestamp(uuid));
            }
        }

        return entity;
    }

    /**
     * 新增saveAll方法
     * 使用时候注意不要插入大量数据,限制5000条内
     *
     * @param dataList 数据对象列表
     * @return dataList
     */
    @Override
    @Transactional
    public List<D> saveAll(List<D> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            log.debug("saveAll noData");
            return dataList;
        }
        List<E> es = dataList.stream().map(this::getEntity).collect(Collectors.toList());
        getRepository().saveAll(es);
        return DaoUtil.convertDataList(es);
    }

    @Override
    @Transactional
    public D saveAndFlush(TenantId tenantId, D domain) {
        D d = save(tenantId, domain);
        getRepository().flush();
        return d;
    }

    @Override
    public D findById(TenantId tenantId, UUID key) {
        log.debug("Get entity by key {}", key);
        Optional<E> entity = getRepository().findById(key);
        return DaoUtil.getData(entity);
    }

    @Override
    public ListenableFuture<D> findByIdAsync(TenantId tenantId, UUID key) {
        log.debug("Get entity by key async {}", key);
        return service.submit(() -> DaoUtil.getData(getRepository().findById(key)));
    }

    @Override
    public boolean existsById(TenantId tenantId, UUID key) {
        log.debug("Exists by key {}", key);
        return getRepository().existsById(key);
    }

    @Override
    public ListenableFuture<Boolean> existsByIdAsync(TenantId tenantId, UUID key) {
        log.debug("Exists by key async {}", key);
        return service.submit(() -> getRepository().existsById(key));
    }

    @Override
    @Transactional
    public boolean removeById(TenantId tenantId, UUID id) {
        getRepository().deleteById(id);
        log.debug("Remove request: {}", id);
        return !getRepository().existsById(id);
    }

    @Transactional
    public void removeAllByIds(Collection<UUID> ids) {
        JpaRepository<E, UUID> repository = getRepository();
        ids.forEach(repository::deleteById);
    }

    @Override
    public List<D> find(TenantId tenantId) {
        List<E> entities = Lists.newArrayList(getRepository().findAll());
        return DaoUtil.convertDataList(entities);
    }
}
