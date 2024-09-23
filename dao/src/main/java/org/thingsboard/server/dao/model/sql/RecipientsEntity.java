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

package org.thingsboard.server.dao.model.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.common.data.id.RecipientsId;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.enume.recipients.TransmissionTypeEnum;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * 接收方网络配置- 数据库对象
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 10:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.RECIPIENTS)
@Slf4j
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class RecipientsEntity extends BaseSqlEntity<Recipients> implements SearchTextEntity<Recipients> {

    /**
     * 租户ID
     */
    @Column(name = ModelConstants.TENANT_ID_COLUMN)
    private UUID tenantId;

    /**
     * 网络传输类型,HTTP Post ,MQTT
     * {@link TransmissionTypeEnum}
     */
    @Column(name = ModelConstants.RECIPIENTS_RULE_TRANSMISSION_TYPE_COLUMN)
    private String transmissionType;

    /**
     * 名称
     */
    @Column(name = ModelConstants.RECIPIENTS_NAME_COLUMN)
    private String name;

    /**
     * 更新时间
     */
    @Column(name = ModelConstants.RECIPIENTS_UPDATED_TIME_COLUMN)
    private long updateTime;

    /**
     * 不同传输类型各种配置,json
     */
    @Type(type = "json")
    @Column(name = ModelConstants.RECIPIENTS_JSON_DATA_COLUMN)
    private Object jsonData;

    /**
     * 查询条件
     */
    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    /**
     * 用户名
     */
    @Column(name = ModelConstants.RECIPIENTS_USERNAME_COLUMN)
    private String username;

    /**
     * 密码
     */
    @Column(name = ModelConstants.RECIPIENTS_PASSWORD_COLUMN)
    private String password;

    public RecipientsEntity(Recipients recipients) {
        if (recipients.getId() != null) {
            this.setUuid(recipients.getUuidId());
        }
        if (recipients.getRecipientsId() != null) {
            this.id = DaoUtil.getId(recipients.getRecipientsId());
        }
        this.setCreatedTime(recipients.getCreatedTime());
        this.setUpdateTime(recipients.getUpdateTime());
        this.setTenantId(recipients.getTenantId());

        this.setName(recipients.getName());
        this.setSearchText(recipients.getSearchText());
        this.setPassword(recipients.getPassword());
        this.setUsername(recipients.getUsername());

        this.setTransmissionType(recipients.getTransmissionType());
        this.setJsonData(recipients.getJsonData());
    }

    @Override
    public Recipients toData() {
        Recipients.RecipientsBuilder recipientsBuilder = Recipients.builder()
                .recipientsId(new RecipientsId(this.getUuid()))
                .tenantId(this.tenantId)
                .name(this.name)
                .updateTime(this.updateTime)
                .transmissionType(this.transmissionType)
                .username(this.username)
                .jsonData(this.jsonData)
                .password(this.password)
                .searchText(this.searchText);
        Recipients build = recipientsBuilder.build();
        build.setCreatedTime(this.createdTime);
        return build;
    }

    @Override
    public String getSearchTextSource() {
        return this.searchText;
    }
}