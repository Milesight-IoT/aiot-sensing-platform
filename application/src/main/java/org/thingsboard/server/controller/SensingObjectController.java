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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.KvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.List;
import java.util.UUID;

import static org.thingsboard.server.controller.ControllerConstants.SORT_ORDER_ALLOWABLE_VALUES;

/**
 * 感知对象
 *
 * @author Yanchuan
 */
@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SensingObjectController extends BaseController {
    /**
     * (MS)获取感知对象列表
     *
     * @param pageSize     每页大小
     * @param page         页码 0开始
     * @param textSearch   查找对象
     * @param sortProperty 要排序的属性  createdTime
     * @param sortOrder    排序顺序  ASC(上升)或DESC(下降)
     * @return
     * @throws Exception
     */
    @ApiOperation("获取感知对象列表")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjects", method = RequestMethod.GET)
    public PageData<SensingObject> getSensingObjects(
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @ApiParam("从0开始的页码")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam("基于名称且不区分大小写")
            @RequestParam(value = "textSearch", required = false) String textSearch,
            @ApiParam(value = "要排序的属性  createdTime")
            @RequestParam(value = "sortProperty", required = false) String sortProperty,
            @ApiParam(value = "排序顺序  ASC(上升)或DESC(下降)", allowableValues = SORT_ORDER_ALLOWABLE_VALUES)
            @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) throws Exception {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return sensingObjectService.findByTenantId(getTenantId(), pageLink);
    }

    /**
     * (MS)创建或修改感知对象
     *
     * @param sensingObject 感知对象
     * @return
     * @throws Exception
     */
    @ApiOperation("创建或修改感知对象")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObject", method = RequestMethod.POST)
    public SensingObject saveSensingObject(@RequestBody SensingObject sensingObject) throws Exception {
        return sensingObjectService.saveSensingObject(getCurrentUser().getTenantId(), sensingObject);
    }

    /**
     * (MS)删除感知对象
     *
     * @param sensingObjectId 感知对象ID
     * @throws ThingsboardException
     */
    @ApiOperation("删除感知对象")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObject/{sensingObjectId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteSensingObject(
            @ApiParam(value = "感知对象ID")
            @PathVariable(value = "sensingObjectId", required = true) String sensingObjectId
    ) throws ThingsboardException {
        sensingObjectService.deleteSensingObject(toUUID(sensingObjectId));
    }

    /**
     * (MS)根据名称获取感知对象
     *
     * @param name 感知名称
     * @return
     * @throws Exception
     */
    @ApiOperation("根据名称获取感知对象")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjectByName", method = RequestMethod.GET)
    public SensingObject getSensingObjectByName(
            @ApiParam("名称")
            @RequestParam("name") String name
    ) throws Exception {
        return sensingObjectService.findByTenantIdAndName(getTenantId(), name);
    }

    /**
     * (MS)分页获取感知对象所有图片通道
     *
     * @param pageSize   一页中的最大实体数
     * @param page       页序号从0开始
     * @param textSearch 查询一级名称
     * @return {@link SensingObject}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjectImages", method = RequestMethod.GET)
    public PageData<SensingObject> findSensingObjectSelectList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "textSearch", required = false) String textSearch,
            @RequestParam(value = "isRoi", required = false) boolean isRoi
    ) throws Exception {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, "", "");
        return sensingObjectService.findSensingObjectImage(getTenantId(), pageLink, isRoi);
    }

    /**
     * (MS)根据设备ID获取具体图片对象
     *
     * @param deviceId 设备ID
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/findActualImage", method = RequestMethod.GET)
    public KvEntry findActualImage(@RequestParam UUID deviceId) throws Exception {
        return sensingObjectService.findTsKvEnTry(getTenantId(), new DeviceId(deviceId));
    }

    /**
     * (MS)获取感知对象-下拉列表
     *
     * @param pageSize     一页中的最大实体数
     * @param page         页序号从0开始
     * @param textSearch   感知通道（模糊查询）。
     * @param needChannels 是否需要sensingChannels数据 true/false
     * @param abilityType  能力类型,1.普通 2.图片
     * @return {@link SensingObject}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjectSelectList", method = RequestMethod.GET)
    public PageData<SensingObject> sensingObjectSelectList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "textSearch", required = false) String textSearch,
            @RequestParam(value = "needChannels", required = false) boolean needChannels,
            @RequestParam(value = "abilityType", required = false) String abilityType,
            @RequestParam(value = "isRoi", required = false) boolean isRoi
    ) throws Exception {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, "", "");
        return sensingObjectService.findSelectListByTenantId(getTenantId(), pageLink, needChannels, abilityType, isRoi);
    }

    /**
     * (MS)ID列表获取感知通道数据
     *
     * @param sensingObjectsIds 感知ID,多个逗号隔开
     * @param needChannels      是否需要sensingChannels数据 true/false
     * @param abilityType       能力类型,1.普通 2.图片
     * @param isRoi             仅需要roi  true/false
     * @return {@link SensingObject}
     */
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjectsByIds", method = RequestMethod.GET)
    public List<SensingObject> findSensingObjectsByIds(
            @RequestParam("sensingObjectsIds") String sensingObjectsIds,
            @RequestParam(value = "needChannels", required = false) boolean needChannels,
            @RequestParam(value = "abilityType", required = false) String abilityType,
            @RequestParam(value = "isRoi", required = false) boolean isRoi
    ) throws ThingsboardException {
        return sensingObjectService.findSensingObjectsByIds(getTenantId(), sensingObjectsIds, needChannels, abilityType, isRoi);
    }
}
