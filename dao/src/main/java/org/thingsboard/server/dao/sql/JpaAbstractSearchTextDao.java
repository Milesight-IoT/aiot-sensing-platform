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

import org.thingsboard.server.dao.model.BaseEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;

/**
 * 带SearchText字段的表DAO
 *
 * @author Valerii Sosliuk
 * @date 5/6/2017
 */
public abstract class JpaAbstractSearchTextDao<E extends BaseEntity<D>, D> extends JpaAbstractDao<E, D> {

    /**
     * 自动设置searchText字段(英文小写)
     *
     * @param entity 数据库实体
     */
    @Override
    protected void setSearchText(E entity) {
        ((SearchTextEntity) entity).setSearchText(((SearchTextEntity) entity).getSearchTextSource().toLowerCase());
    }
}
