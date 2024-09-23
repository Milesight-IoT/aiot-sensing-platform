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

package org.thingsboard.server.dao.timeseries;

import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recognition.TelemetryRecognition;
import org.thingsboard.server.common.data.recognition.TelemetryRecognitionReceipt;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * 感知数据 - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/8/9 9:13
 */
public interface TelemetryRecognitionService {
    /**
     * 获取感知数据列表
     *
     * @param deviceId 设备ID(UUID)
     * @param ability  能力名
     * @param startTs  开始Ts
     * @param endTs    结束Ts
     * @param pageLink 分页信息
     * @return {@link TelemetryRecognition}
     */
    PageData<TelemetryRecognition> findTelemetryRecognition(UUID deviceId, String ability, long startTs, long endTs, PageLink pageLink);

    /**
     * 保存感知数据
     *
     * @param telemetryRecognition 感知数据
     * @return {@link TelemetryRecognition}
     */
    TelemetryRecognition saveTelemetryRecognition(TelemetryRecognition telemetryRecognition);

    /**
     * 删除设备感知数据
     *
     * @param deviceId 删除设备感知数据
     */
    void deleteByDeviceId(UUID deviceId);

    /**
     * 保存第三方感知结果
     *
     * @param telemetryRecognitionList 感知数据结果列表
     * @return boolean
     * @throws ThingsboardException e
     * @throws ExecutionException   e
     * @throws InterruptedException e
     */
    Boolean saveForeignTelemetryRecognition(List<TelemetryRecognitionReceipt> telemetryRecognitionList) throws ThingsboardException, ExecutionException, InterruptedException;

}
