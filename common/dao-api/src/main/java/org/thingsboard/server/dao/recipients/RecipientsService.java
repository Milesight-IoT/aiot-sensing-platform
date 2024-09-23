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

package org.thingsboard.server.dao.recipients;

import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.recipients.RecipientsDto;
import org.thingsboard.server.common.data.rule.RuleNode;

import java.util.List;
import java.util.UUID;

/**
 * 接收方网络配置管理 - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:06
 */
public interface RecipientsService {
    /**
     * 查找接收方管理列表
     *
     * @param tenantId 租户ID
     * @param pageLink 分页条件
     * @return {@link Recipients}
     */
    PageData<Recipients> findRecipientsList(TenantId tenantId, PageLink pageLink);


    /**
     * 创建或更新
     *
     * @param tenantId 租户ID
     * @param dto      接收方对象
     * @return {@link Recipients}
     * @throws ThingsboardException 查询异常
     */
    Recipients createOrUpdate(TenantId tenantId, RecipientsDto dto) throws ThingsboardException;

    /**
     * 删除
     *
     * @param tenantId     租户ID
     * @param recipientsId 主键ID
     * @return
     * @throws ThingsboardException 异常
     */
    List<RuleChainId> deleteRecipients(TenantId tenantId, UUID recipientsId) throws ThingsboardException;

    /**
     * 获取详情
     *
     * @param tenantId     租户ID
     * @param recipientsId 主键ID
     * @return {@link Recipients}
     * @throws ThingsboardException 异常
     */
    Recipients getRecipientsById(TenantId tenantId, UUID recipientsId) throws ThingsboardException;

    /**
     * 批量查找查找
     *
     * @param uuidList 主键ID数组
     * @return {@link Recipients}
     */
    List<Recipients> findByIds(List<UUID> uuidList);

    /**
     * （MS）根据名称获取详情
     *
     * @param name 名称
     */
    Recipients getByTenantIdAndName(TenantId tenantId, String name);

    /**
     * 设置节点属性
     *
     * @param recipients     接收方
     * @param recipientsNode 节点
     */
    void setConfigurationAndType(Recipients recipients, RuleNode recipientsNode);


    /**
     * (MS) ID列表获取第三方数据
     *
     * @param recipientsIds 主键ID,多个逗号隔开
     * @return {@link Recipients}
     */
    List<Recipients> findRecipientsByIds(String recipientsIds);
}
