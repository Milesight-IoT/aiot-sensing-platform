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

package org.thingsboard.server.common.data.customWidget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhangzy
 * @Data 2023.04.14
 * 获取前端传参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetDTO {

    /**
     * 宽
     */
    private int sizeX;
    /**
     * 高
     */
    private int sizeY;
    /**
     * 行
     */
    private int row;
    /**
     * 列
     */
    private int col;
    /**
     * 部件类型
     */
    private String type;

    /**
     * type为Snapshot preview时候的传参
     */
    private SensingObjectImage sensingObjectImage;
}
