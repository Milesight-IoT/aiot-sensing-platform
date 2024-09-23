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

package org.thingsboard.server.common.data.kv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * @author zhang.zy
 * 批量删除
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteTsKv {

    protected String entityType;

    protected List<UUID> entityIds;

    protected String key;

    private long startTs;

    private long endTs;

    private List<Long> partitions;

    private int partitionIndex;


    public BatchDeleteTsKv(String entityType, List<UUID> entityIds, String key, List<Long> partitions) {
        this.entityType = entityType;
        this.entityIds = entityIds;
        this.key = key;
        this.partitions = partitions;
        this.partitionIndex = partitions.size() - 1;
    }

    public boolean hasNextPartition() {
        return partitionIndex >= 0;
    }

    public long getNextPartition() {
        long partition = partitions.get(partitionIndex);
        partitionIndex--;
        return partition;
    }

}
