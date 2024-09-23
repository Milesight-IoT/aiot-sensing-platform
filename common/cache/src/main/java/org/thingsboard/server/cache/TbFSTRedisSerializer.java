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
package org.thingsboard.server.cache;

import org.springframework.data.redis.serializer.SerializationException;
import org.thingsboard.server.common.data.FSTUtils;

public class TbFSTRedisSerializer<K, V> implements TbRedisSerializer<K, V> {

    @Override
    public byte[] serialize(V value) throws SerializationException {
        return FSTUtils.encode(value);
    }

    @Override
    public V deserialize(K key, byte[] bytes) throws SerializationException {
        return FSTUtils.decode(bytes);
    }
}
