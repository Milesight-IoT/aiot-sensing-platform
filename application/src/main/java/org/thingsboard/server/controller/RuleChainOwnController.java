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

import lombok.extern.slf4j.Slf4j;
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
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.rule.own.RuleChainOwn;
import org.thingsboard.server.common.data.rule.own.RuleChainOwnDto;
import org.thingsboard.server.dao.rule.RuleChainOwnService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 规则链表（二开）
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/3/31 13:30
 */
@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/ruleChainOwn")
public class RuleChainOwnController extends BaseController {

    @Resource
    private RuleChainOwnService ruleChainOwnService;

    /**
     * （MS）列表
     *
     * @param pageSize     一页中的最大实体数
     * @param page         页序号从0开始
     * @param textSearch   规则链名称（模糊查询）。
     * @param sortProperty 要排序的实体的属性 createdTime, name
     * @param sortOrder    排序. ASC or DESC
     * @return {@link PageData<RuleChainOwn>}
     */
    @ResponseBody
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChains", method = RequestMethod.GET)
    public PageData<RuleChainOwn> findTenantRuleChainsOwnList(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder
    ) throws ThingsboardException {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return ruleChainOwnService.findTenantRuleChainOwnList(getTenantId(), pageLink);
    }

    /**
     * （MS）创建或更新
     *
     * @param dto 接收方对象
     * @return {@link RuleChainOwn}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/createOrUpdate", method = RequestMethod.POST)
    public RuleChainOwn createOrUpdate(@Valid @RequestBody RuleChainOwnDto dto) throws Exception {
        RuleChainOwn ruleChainOwn = ruleChainOwnService.createOrUpdate(getCurrentUser().getTenantId(), dto);
        refreshRuleChain(ruleChainOwn.getRuleChainId());
        return ruleChainOwn;
    }

    /**
     * （MS）删除
     *
     * @param ruleChainOwnId 主键ID
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/{ruleChainOwnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void delete(@PathVariable(value = "ruleChainOwnId") String ruleChainOwnId) throws ThingsboardException {
        RuleChainOwn ruleChainOwn = ruleChainOwnService.deleteRuleChainOwn(getCurrentUser().getTenantId(), toUUID(ruleChainOwnId));
        refreshRuleChain(ruleChainOwn.getRuleChainId());
    }


    /**
     * （MS）获取详情
     *
     * @param ruleChainOwnId 主键ID
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/{ruleChainOwnId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public RuleChainOwn getRuleChainOwnById(@PathVariable(value = "ruleChainOwnId") String ruleChainOwnId) throws ThingsboardException {
        return ruleChainOwnService.getRuleChainOwnById(getCurrentUser().getTenantId(), toUUID(ruleChainOwnId));
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
    public RuleChainOwn getByTenantIdAndName(@RequestParam("name") String name) throws ThingsboardException {
        return ruleChainOwnService.getByTenantIdAndName(getCurrentUser().getTenantId(), name);
    }

    /**
     * （MS）获取第三方关联规则链列表
     *
     * @param recipientsId 第三方主键
     * @return {@link RuleChainOwn}
     * @throws ThingsboardException 异常
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/findRuleChainOwnByRecipientId/{recipientsId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public List<RuleChainOwn> findRuleChainOwnByRecipientId(@PathVariable(value = "recipientsId") String recipientsId) throws ThingsboardException {
        return ruleChainOwnService.findRuleChainOwnByRecipientId(getCurrentUser().getTenantId(), recipientsId);
    }
}