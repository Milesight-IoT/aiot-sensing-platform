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
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.device.RoiSelectData;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

import java.util.List;

import static org.thingsboard.server.controller.ControllerConstants.DEVICE_ID_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.SORT_ORDER_ALLOWABLE_VALUES;

/**
 * 设备能力
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceAbilityController extends BaseController {

    @ApiOperation("获取设备能力列表")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/deviceAbilities", method = RequestMethod.GET)
    public PageData<DeviceAbility> getDeviceAbilities(
            @ApiParam("设备ID")
            @RequestParam(value = "deviceId", required = true) String deviceId,
            @ApiParam("能力类型 0-所有; 1-基础能力; 2-图片能力")
            @RequestParam(value = "abilityType", required = false, defaultValue = "0") short abilityType,
            @ApiParam("一页中的最大数量")
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @ApiParam("从0开始的页码")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(value = "要排序的属性  createdTime")
            @RequestParam(value = "sortProperty", required = false) String sortProperty,
            @ApiParam(value = "排序顺序  ASC（上升）或DESC（下降）", allowableValues = SORT_ORDER_ALLOWABLE_VALUES)
            @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) throws ThingsboardException {
        PageLink pageLink = createPageLink(pageSize, page, null, sortProperty, sortOrder);
        return deviceAbilityService.findDeviceAbilitiesByDeviceIdAndAbilityType(toUUID(deviceId), abilityType, pageLink);
    }

    @ApiOperation("新增设备能力")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/deviceAbility", method = RequestMethod.POST)
    public DeviceAbility saveDeviceAbility(@RequestBody DeviceAbility deviceAbility) {
        return deviceAbilityService.saveDeviceAbilityAndCache(deviceAbility);
    }

    @ApiOperation("获取设备能力关联的感知对象")
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/sensingObjectsByAbilityId/{deviceAbilityId}", method = RequestMethod.GET)
    @ResponseBody
    public SensingObject getSensingObjectsByDeviceAbilityId(
            @ApiParam(value = DEVICE_ID_PARAM_DESCRIPTION)
            @PathVariable("deviceAbilityId") String strDeviceAbilityId) throws ThingsboardException {
        return sensingObjectService.findByDeviceAbilityId(toUUID(strDeviceAbilityId));
    }

    @ApiOperation("删除设备能力")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/deviceAbility/{deviceAbilityId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteDeviceAbility(
            @ApiParam(value = "设备能力ID")
            @PathVariable(value = "deviceAbilityId", required = true) String deviceAbilityId
    ) throws ThingsboardException {
        deviceAbilityService.deleteDeviceAbility(toUUID(deviceAbilityId));
    }

    @ApiOperation("根据设备ID和能力值获取设备能力")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/deviceAbilityByDeviceIdAndAbility", method = RequestMethod.GET)
    public DeviceAbility getDeviceAbilityByDeviceIdAndAbility(
            @ApiParam("设备ID")
            @RequestParam(value = "deviceId", required = true) String deviceId,
            @ApiParam("能力")
            @RequestParam(value = "ability", required = true) String ability
    ) throws ThingsboardException {
        return deviceAbilityService.findDeviceAbilityByDeviceIdAndAbility(toUUID(deviceId), ability);
    }

    /**
     * （MS）获取设备-ROI下拉列表
     *
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param textSearch  查询一级设备
     * @param pageSize    每页数量
     * @param page        页码
     * @param isRoi       是否只需要ROI true、false
     * @return {@link RoiSelectData}
     * @throws ThingsboardException 异常
     */
    @ApiOperation("获取设备-ROI下拉列表")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/selectRoiAbilities", method = RequestMethod.GET)
    public PageData<RoiSelectData> findRoiSelectList(
            @RequestParam(value = "abilityType", required = false, defaultValue = "0") short abilityType,
            @RequestParam(value = "textSearch", required = false) String textSearch,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "isRoi", required = false) boolean isRoi
    ) throws ThingsboardException {
        PageLink pageLink = new PageLink(pageSize, page, textSearch);
        return deviceAbilityService.findRoiSelectList(getTenantId(), abilityType, pageLink, isRoi);
    }

    /**
     * （MS）获取设备-ROI下拉详情列表
     *
     * @param deviceIds   设备id多个逗号隔开
     * @param abilityType 能力类型 0-所有; 1-基础能力; 2-图片能力
     * @param isRoi       是否只需要ROI true、false
     * @return {@link RoiSelectData}
     * @throws ThingsboardException 异常
     */
    @ApiOperation("获取设备-ROI下拉详情列表")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/detailsRoiAbilities", method = RequestMethod.GET)
    public List<RoiSelectData> findRoiSelectByDeviceList(
            @RequestParam(value = "deviceIds") String deviceIds,
            @RequestParam(value = "abilityType", required = false, defaultValue = "0") Short abilityType,
            @RequestParam(value = "isRoi", required = false) boolean isRoi
    ) throws ThingsboardException {
        return deviceAbilityService.findRoiSelectByDeviceList(getTenantId(), deviceIds, abilityType, isRoi);
    }
}
