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

package org.thingsboard.server.common.data.recipients;

import lombok.Data;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.enume.recipients.TransmissionTypeEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 接收方网络配置管理 - 创建更新实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:32
 */
@Data
public class RecipientsDto implements Serializable {

    /**
     * 主键ID EntityType.RECIPIENTS
     * 创建为空,更新不为空
     */
    private RecipientsId recipientsId;


    /**
     * 网络传输类型,HTTP Post ,MQTT
     * {@link TransmissionTypeEnum}
     */
    @NotBlank(message = "Network transport type cannot be empty")
    private String transmissionType;

    /**
     * 名称
     */
    @NotBlank(message = "Name is required")
    @Size(max = 32, message = "transmissionType abnormal length max 32 char")
    private String name;

    /**
     * 配置参数,不同传输类型配置
     * transmissionType = HTTP post
     * {
     *     "url":"http://****:**" //请求地址
     * }
     * transmissionType = MQTT
     * {
     *     "host":"127.0.0.1", //ip
     *     "port":"1883", //端口
     *     "topic":"topic" //主题
     * }
     */
    @NotNull(message = "jsonData cannot be empty")
    private Object jsonData;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
