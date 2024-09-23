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

package org.thingsboard.server.common.data.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ROI下拉选项 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/8/10 9:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoiSelectData implements Serializable {
    /**
     * 设备ID
     */
    private String deviceId;
    /**
     * 设备名
     */
    private String deviceName;
    /**
     * 二级ROI对象
     */
    private List<RoiChildSelectData> childSelectData = new ArrayList<>();
}
