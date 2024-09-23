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

package org.thingsboard.server.cache.ms;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Luohh
 */
public abstract class MsAbstractCache<K, V> {
    private final int maxSize;
    private final Map<K, CacheEntry<V>> cache;

    public MsAbstractCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > MsAbstractCache.this.maxSize;
            }
        };
    }

    public void put(K key, V value, long timeToLiveInSeconds) {
        LocalDateTime expirationTime = LocalDateTime.now().plus(timeToLiveInSeconds, ChronoUnit.SECONDS);
        cache.put(key, new CacheEntry<>(value, expirationTime));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && entry.hasExpired()) {
            cache.remove(key);
            entry = null;
        }
        return (entry != null) ? entry.getValue() : null;
    }

    public void clear() {
        cache.clear();
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return cache.size();
    }

    private static class CacheEntry<V> {
        private final V value;
        private final LocalDateTime expirationTime;

        public CacheEntry(V value, LocalDateTime expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public boolean hasExpired() {
            return expirationTime.isBefore(LocalDateTime.now());
        }

        public V getValue() {
            return value;
        }
    }
}
