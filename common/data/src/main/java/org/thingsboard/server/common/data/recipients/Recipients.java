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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.data.SearchTextBased;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.id.RuleChainId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 接收方网络配置管理表 - 实体
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 10:24
 */
@ApiModel
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipients extends SearchTextBased<RecipientsId> {

    /**
     * 主键ID
     */
    private RecipientsId recipientsId;

    /**
     * 租户ID
     */
    private UUID tenantId;

    /**
     * 网络传输类型,HTTP Post ,MQTT
     */
    private String transmissionType;

    /**
     * 名称
     */
    private String name;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 配置参数,不同传输类型配置
     */
    private Object jsonData;

    /**
     * 查询条件
     */
    @JsonIgnore
    private String searchText;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
    /**
     * 规则链ID数组
     */
    @JsonIgnore
    private List<RuleChainId> ruleChainIds = new ArrayList<>();

    @Override
    public String getSearchText() {
        return getName();
    }
}