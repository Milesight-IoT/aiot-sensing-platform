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

package org.thingsboard.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.recipients.Recipients;
import org.thingsboard.server.common.data.recipients.RecipientsDto;
import org.thingsboard.server.dao.recipients.RecipientsService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 接收方管理
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/12 11:12
 */
@RestController
@TbCoreComponent
@RequestMapping("/api/recipients")
public class RecipientsController extends BaseController {
    @Resource
    private RecipientsService recipientsService;

    /**
     * （MS）列表
     *
     * @param pageSize     一页中的最大实体数
     * @param page         页序号从0开始
     * @param textSearch   名称（模糊查询）。
     * @param sortProperty 要排序的实体的属性 createdTime, name
     * @param sortOrder    排序. ASC or DESC
     * @return {@link Recipients}
     */
    @ResponseBody
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/findRecipientsList", method = RequestMethod.GET)
    public PageData<Recipients> findRecipientsList(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder
    ) throws ThingsboardException {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return recipientsService.findRecipientsList(getTenantId(), pageLink);
    }

    /**
     * （MS）创建或更新
     *
     * @param dto 接收方对象
     * @return {@link Recipients}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/createOrUpdate", method = RequestMethod.POST)
    public Recipients createOrUpdate(@Valid @RequestBody RecipientsDto dto) throws Exception {
        TenantId tenantId = getTenantId();
        Recipients orUpdate = recipientsService.createOrUpdate(tenantId, dto);
        refreshRuleChain(tenantId, orUpdate.getRuleChainIds());
        return orUpdate;
    }

    /**
     * （MS）删除
     *
     * @param recipientsId 主键ID
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/{recipientsId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteRecipients(@PathVariable(value = "recipientsId") String recipientsId) throws ThingsboardException {
        List<RuleChainId> ruleChainIds = recipientsService.deleteRecipients(getCurrentUser().getTenantId(), toUUID(recipientsId));
        refreshRuleChain(getTenantId(), ruleChainIds);
    }

    /**
     * （MS）获取详情
     *
     * @param recipientsId 主键ID
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/{recipientsId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Recipients getRecipients(@PathVariable(value = "recipientsId") String recipientsId) throws ThingsboardException {
        return recipientsService.getRecipientsById(getCurrentUser().getTenantId(), toUUID(recipientsId));
    }

    /**
     * （MS）根据名称获取详情
     *
     * @param name 名称
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/detailsByName", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Recipients getByTenantIdAndName(@RequestParam("name") String name) throws ThingsboardException {
        return recipientsService.getByTenantIdAndName(getCurrentUser().getTenantId(), name);
    }

    /**
     *  (MS) ID列表获取第三方数据
     *
     * @param recipientsIds 主键ID,多个逗号隔开
     * @return {@link SensingObject}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/recipientsIds", method = RequestMethod.GET)
    public List<Recipients> findRecipientsByIds(@RequestParam("recipientsIds") String recipientsIds) {
        return recipientsService.findRecipientsByIds(recipientsIds);
    }
}