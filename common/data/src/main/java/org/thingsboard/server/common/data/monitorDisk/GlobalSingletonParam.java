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

package org.thingsboard.server.common.data.monitorDisk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhang.zy
 * date 2023
 */
@Data
@AllArgsConstructor
public class GlobalSingletonParam {
    private static volatile GlobalSingletonParam instance;

    /**
     * 执行删除到下一次执行删除期间,总新增遥测数据量
     */
    private AtomicInteger addCountNum;
    /**
     * 删除的数据量
     */
    private AtomicInteger deleteNum;
    /**
     * 上次磁盘删除时候,磁盘空间大小
     */
    private AtomicLong lastRemainDisk;
    /**
     * 删除执行次数
     */
    private AtomicInteger numberOfDeletionExecutions;
    /**
     * 需要删除的次数
     */
    private int numberOfTimesNeedsToBeDeleted;

    private boolean isCanSaveData;

    /**
     * 磁盘可用空间 G
     */
    long cassandraSpaceUsage;
    /**
     * 磁盘总空间
     */
    long cassandraSpaceTotal;

    private GlobalSingletonParam() {
        addCountNum = new AtomicInteger(0);
        deleteNum = new AtomicInteger(0);
        lastRemainDisk = new AtomicLong(0);
        numberOfDeletionExecutions = new AtomicInteger(0);
        isCanSaveData = true;
        numberOfTimesNeedsToBeDeleted = 0;
    }


    public static synchronized GlobalSingletonParam getInstance() {
        if (instance == null) {
            instance = new GlobalSingletonParam();
        }
        return instance;
    }

    public void addNum() {
        if (getInstance().getLastRemainDisk().get() == 0) {
            // 没有执行过删除所以不需要记录添加的addCount
            return;
        }
        getInstance().getAddCountNum().getAndAdd(1);
    }

    public void deleteAddNum(long cassandraSpaceUsage) {
        GlobalSingletonParam param = getInstance();
        if (param.getLastRemainDisk().get() == 0) {
            param.getAddCountNum().set(0);
        } else if (cassandraSpaceUsage >= param.getLastRemainDisk().get()) {
            int deleteNum = param.getDeleteNum().get();
            int addNum = param.getAddCountNum().get();
            param.getAddCountNum().set(Math.max(addNum - deleteNum, 0));
        }
    }

    public void reset() {
        addCountNum = new AtomicInteger(0);
        deleteNum = new AtomicInteger(0);
        lastRemainDisk = new AtomicLong(0);
        numberOfDeletionExecutions = new AtomicInteger(0);
        isCanSaveData = true;
        numberOfTimesNeedsToBeDeleted = 0;
    }
}
