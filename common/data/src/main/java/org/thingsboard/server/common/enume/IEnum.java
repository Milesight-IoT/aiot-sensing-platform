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

package org.thingsboard.server.common.enume;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * 枚举接口
 *
 * @author Luohh
 * @date 2023/4/3
 */
public interface IEnum<K> {

    /**
     * 值
     *
     * @return 值
     */
    K getValue();

    /**
     * 标签
     *
     * @return 标签
     */
    String getLabel();

    /**
     * 根据值获取枚举对象
     *
     * @param clazz 枚举类
     * @param value 值
     * @return clazzEnum
     */
    static <T extends IEnum> T of(@NotNull Class<T> clazz, Object value) {
        return Stream.of(clazz.getEnumConstants()).filter(ele -> ele.getValue().equals(value)).findFirst().orElse(null);
    }

    /**
     * 根据标签枚举对象
     *
     * @param clazz 枚举类
     * @param label 标签
     * @return clazzEnum
     */
    static <T extends IEnum> T labelOf(@NotNull Class<T> clazz, String label) {
        return Stream.of(clazz.getEnumConstants()).filter(ele -> ele.getLabel().equals(label)).findFirst().orElse(null);
    }

    /**
     * 比较值
     *
     * @param value 值
     * @return 是否一致
     */
    default boolean eq(K value) {
        return getValue().equals(value);
    }

}