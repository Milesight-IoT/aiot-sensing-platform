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

package org.thingsboard.server.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.entity.AbstractEntityService;

import java.util.UUID;

/**
 * 服务增强
 *
 * @author Luohh
 */
@Slf4j
public class BaseDaoServiceImpl<D extends Dao<T>, T> extends AbstractEntityService {
    @Autowired
    public D baseDao;

    public T findByIdOrThrow(TenantId tenantId, UUID id) throws ThingsboardException {
        T byId = baseDao.findById(tenantId, id);
        if (byId == null) {
            log.error("dao = {} , id = {} not found db object", baseDao.getClass(), id);
            throw new ThingsboardException("data not found , does not exist!", ThingsboardErrorCode.GENERAL);
        }
        return byId;
    }

    public void checkParameter(String name, String param) throws ThingsboardException {
        if (StringUtils.isEmpty(param)) {
            log.error("Error converting from error: Parameter name = {} can't be empty! ", name);
            throw new ThingsboardException("Parameter '" + name + "' can't be empty!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    public <E> E convertValue(String errorMsg, Object fromValue, Class<E> toValueType) throws ThingsboardException {
        try {
            return JacksonUtil.OBJECT_MAPPER.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            log.error("Error converting from error:", e);
            throw new ThingsboardException("Parameter '" + errorMsg + "' Error!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    public <E> E convertValueUnknownProperties(Object fromValue, Class<E> toValueType) {
        return JacksonUtil.FAIL_ON_UNKNOWN_PROPERTIES_MAPPER.convertValue(fromValue, toValueType);
    }

    public T save(TenantId tenantId, T entity) {
        return baseDao.save(tenantId, entity);
    }

    public T saveAndFlush(TenantId tenantId, T entity) {
        return baseDao.saveAndFlush(tenantId, entity);
    }
}
