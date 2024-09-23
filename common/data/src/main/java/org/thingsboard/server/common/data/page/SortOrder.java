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
package org.thingsboard.server.common.data.page;

import lombok.Data;

import java.io.Serializable;

/**
 * 排序对象
 *
 * @author Andrew Shvayka
 */
@Data
public class SortOrder implements Serializable {
    /**
     * 排序字段
     */
    private String property;
    /**
     * 排序规则 ASC（正序）, DESC（倒序）
     */
    private Direction direction;

    public SortOrder() {
    }

    public SortOrder(String property) {
        this(property, Direction.ASC);
    }

    public SortOrder(String property, Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public enum Direction {
        /**
         * 排序规则 ASC（正序）, DESC（倒序）
         */
        ASC, DESC
    }

}
