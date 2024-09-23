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

package org.thingsboard.server.dao.rule;

import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.common.data.rule.own.RuleChainOwnDto;

import java.util.List;
import java.util.UUID;

/**
 * 规则链表（二开） - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/3 10:58
 */
public interface RuleChainOwnService {

    /**
     * 规则链（二开）列表
     *
     * @param tenantId 租户ID
     * @param pageLink 分页、查询参数
     * @return {@link PageData<RuleChainOwn>}
     */
    PageData<RuleChainOwn> findTenantRuleChainOwnList(TenantId tenantId, PageLink pageLink);

    /**
     * （二开）创建或更新
     *
     * @param dto      接收方对象
     * @param tenantId 租户ID
     * @return {@link RuleChainOwn}
     * @throws ThingsboardException 异常
     */
    RuleChainOwn createOrUpdate(TenantId tenantId, RuleChainOwnDto dto) throws ThingsboardException;

    /**
     * （MS）删除
     *
     * @param primaryId 主键ID
     * @param tenantId  租户ID
     * @return {@link RuleChainOwn}
     * @throws ThingsboardException 异常
     */
    RuleChainOwn deleteRuleChainOwn(TenantId tenantId, UUID primaryId) throws ThingsboardException;

    /**
     * （MS）获取详情
     *
     * @param primaryId 主键ID
     * @param tenantId  租户ID
     * @return {@link RuleChainOwn}
     * @throws ThingsboardException 异常
     */
    RuleChainOwn getRuleChainOwnById(TenantId tenantId, UUID primaryId) throws ThingsboardException;

    /**
     * （MS）根据名称获取详情
     *
     * @param tenantId 租户ID
     * @param name     名称
     * @return {@link RuleChainOwn}
     */
    RuleChainOwn getByTenantIdAndName(TenantId tenantId, String name);

    /**
     * 删除第三方的节点
     *
     * @param tenantId   租户ID
     * @param recipients 第三方对象
     */
    List<RuleChainId> deleteRecipients(TenantId tenantId, Recipients recipients);

    /**
     * (MS) 获取第三方关联规则链列表
     *
     * @param recipientsId 第三方主键
     * @return {@link RuleChainOwn}
     * @throws ThingsboardException 异常
     */
    List<RuleChainOwn> findRuleChainOwnByRecipientId(TenantId tenantId, String recipientsId);
}
