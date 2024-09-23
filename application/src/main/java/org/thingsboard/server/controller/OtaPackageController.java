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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.OtaPackage;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.SaveOtaPackageInfoRequest;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.ota.ChecksumAlgorithm;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.ota.TbOtaPackageService;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.thingsboard.server.controller.ControllerConstants.DEVICE_PROFILE_ID_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_CHECKSUM_ALGORITHM_ALLOWABLE_VALUES;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_ID_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_INFO_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_SORT_PROPERTY_ALLOWABLE_VALUES;
import static org.thingsboard.server.controller.ControllerConstants.OTA_PACKAGE_TEXT_SEARCH_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.PAGE_DATA_PARAMETERS;
import static org.thingsboard.server.controller.ControllerConstants.PAGE_NUMBER_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.PAGE_SIZE_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.SORT_ORDER_ALLOWABLE_VALUES;
import static org.thingsboard.server.controller.ControllerConstants.SORT_ORDER_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.SORT_PROPERTY_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_AUTHORITY_PARAGRAPH;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;
import static org.thingsboard.server.controller.ControllerConstants.UUID_WIKI_LINK;

/**
 * OTA升级
 *
 * @author Andrew Shvayka
 * @version 1.0
 * @date 2019-11-13
 */
@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
public class OtaPackageController extends BaseController {

    private final TbOtaPackageService tbOtaPackageService;

    public static final String OTA_PACKAGE_ID = "otaPackageId";
    public static final String CHECKSUM_ALGORITHM = "checksumAlgorithm";

    @ApiOperation(value = "Download OTA Package (downloadOtaPackage)", notes = "Download OTA Package based on the provided OTA Package Id." + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority( 'TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage/{otaPackageId}/download", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> downloadOtaPackage(
            HttpServletRequest request,
            @ApiParam(value = OTA_PACKAGE_ID_PARAM_DESCRIPTION)
            @PathVariable(OTA_PACKAGE_ID) String strOtaPackageId) throws ThingsboardException {
        checkParameter(OTA_PACKAGE_ID, strOtaPackageId);
        OtaPackageId otaPackageId = new OtaPackageId(toUUID(strOtaPackageId));
        OtaPackage otaPackage = checkOtaPackageId(otaPackageId, Operation.READ);

        if (otaPackage.hasUrl()) {
            return ResponseEntity.badRequest().build();
        }
        // ota包名 URL编码
        String encodeFileName = URLEncoder.encode(otaPackage.getFileName(), StandardCharsets.UTF_8);

        ByteArrayResource resource = new ByteArrayResource(otaPackage.getData().array());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodeFileName)
                .header("x-filename", encodeFileName)
                .contentLength(resource.contentLength())
                .contentType(parseMediaType(otaPackage.getContentType()))
                .body(resource);
    }


    @ApiOperation(value = "Get OTA Package Info (getOtaPackageInfoById)",
            notes = "Fetch the OTA Package Info object based on the provided OTA Package Id. " +
                    OTA_PACKAGE_INFO_DESCRIPTION + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/otaPackage/info/{otaPackageId}", method = RequestMethod.GET)
    @ResponseBody
    public OtaPackageInfo getOtaPackageInfoById(@ApiParam(value = OTA_PACKAGE_ID_PARAM_DESCRIPTION)
                                                @PathVariable(OTA_PACKAGE_ID) String strOtaPackageId) throws ThingsboardException {
        checkParameter(OTA_PACKAGE_ID, strOtaPackageId);
        OtaPackageId otaPackageId = new OtaPackageId(toUUID(strOtaPackageId));
        return checkNotNull(otaPackageService.findOtaPackageInfoById(getTenantId(), otaPackageId));
    }

    @ApiOperation(value = "Get OTA Package (getOtaPackageById)",
            notes = "Fetch the OTA Package object based on the provided OTA Package Id. " +
                    "The server checks that the OTA Package is owned by the same tenant. " + OTA_PACKAGE_DESCRIPTION + TENANT_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage/{otaPackageId}", method = RequestMethod.GET)
    @ResponseBody
    public OtaPackage getOtaPackageById(@ApiParam(value = OTA_PACKAGE_ID_PARAM_DESCRIPTION)
                                        @PathVariable(OTA_PACKAGE_ID) String strOtaPackageId) throws ThingsboardException {
        checkParameter(OTA_PACKAGE_ID, strOtaPackageId);
        OtaPackageId otaPackageId = new OtaPackageId(toUUID(strOtaPackageId));
        return checkOtaPackageId(otaPackageId, Operation.READ);
    }

    @ApiOperation(value = "Create Or Update OTA Package Info (saveOtaPackageInfo)",
            notes = "Create or update the OTA Package Info. When creating OTA Package Info," +
                    " platform generates OTA Package id as " + UUID_WIKI_LINK +
                    "The newly created OTA Package id will be present in the response. " +
                    "Specify existing OTA Package id to update the OTA Package Info. " +
                    "Referencing non-existing OTA Package Id will cause 'Not Found' error. " +
                    "\n\nOTA Package combination of the title with the version is unique in the scope of tenant. " + TENANT_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage", method = RequestMethod.POST)
    @ResponseBody
    public OtaPackageInfo saveOtaPackageInfo(@ApiParam(value = "A JSON value representing the OTA Package.")
                                             @RequestBody SaveOtaPackageInfoRequest otaPackageInfo) throws ThingsboardException {
        otaPackageInfo.setTenantId(getTenantId());
        otaPackageInfo.setVersion(otaPackageInfo.getTitle());
        checkEntity(otaPackageInfo.getId(), otaPackageInfo, Resource.OTA_PACKAGE);

        return tbOtaPackageService.save(otaPackageInfo, getCurrentUser());
    }

    @ApiOperation(value = "Save OTA Package data (saveOtaPackageData)",
            notes = "Update the OTA Package. Adds the date to the existing OTA Package Info" + TENANT_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE,
            consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage/{otaPackageId}", method = RequestMethod.POST, consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public OtaPackageInfo saveOtaPackageData(@ApiParam(value = OTA_PACKAGE_ID_PARAM_DESCRIPTION)
                                             @PathVariable(OTA_PACKAGE_ID) String strOtaPackageId,
                                             @ApiParam(value = "OTA Package checksum. For example, '0xd87f7e0c'")
                                             @RequestParam(required = false) String checksum,
                                             @ApiParam(value = "OTA Package checksum algorithm.", allowableValues = OTA_PACKAGE_CHECKSUM_ALGORITHM_ALLOWABLE_VALUES)
                                             @RequestParam(value = CHECKSUM_ALGORITHM, required = false, defaultValue = "CRC32") String checksumAlgorithmStr,
                                             @ApiParam(value = "OTA Package data.")
                                             @RequestPart MultipartFile file) throws Exception {
        checkParameter(OTA_PACKAGE_ID, strOtaPackageId);
        checkParameter(CHECKSUM_ALGORITHM, checksumAlgorithmStr);
        OtaPackageId otaPackageId = new OtaPackageId(toUUID(strOtaPackageId));
        OtaPackageInfo otaPackageInfo = checkOtaPackageInfoId(otaPackageId, Operation.READ);
        if (otaPackageInfo.getDistributeStatus() == null) {
            otaPackageInfo.setDistributeStatus(false);
        }
        ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.valueOf(checksumAlgorithmStr.toUpperCase());
        byte[] data = file.getBytes();
        return tbOtaPackageService.saveOtaPackageData(otaPackageInfo, checksum, checksumAlgorithm,
                data, file.getOriginalFilename(), file.getContentType(), getCurrentUser());
    }

    @ApiOperation(value = "Get OTA Package Infos (getOtaPackages)",
            notes = "Returns a page of OTA Package Info objects owned by tenant. " +
                    PAGE_DATA_PARAMETERS + OTA_PACKAGE_INFO_DESCRIPTION + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/otaPackages", method = RequestMethod.GET)
    @ResponseBody
    public PageData<OtaPackageInfo> getOtaPackages(@ApiParam(value = PAGE_SIZE_DESCRIPTION, required = true)
                                                   @RequestParam(required = false, defaultValue = "100") int pageSize,
                                                   @ApiParam(value = PAGE_NUMBER_DESCRIPTION, required = true)
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @ApiParam(value = OTA_PACKAGE_TEXT_SEARCH_DESCRIPTION)
                                                   @RequestParam(required = false) String textSearch,
                                                   @ApiParam(value = SORT_PROPERTY_DESCRIPTION, allowableValues = OTA_PACKAGE_SORT_PROPERTY_ALLOWABLE_VALUES)
                                                   @RequestParam(required = false) String sortProperty,
                                                   @ApiParam(value = SORT_ORDER_DESCRIPTION, allowableValues = SORT_ORDER_ALLOWABLE_VALUES)
                                                   @RequestParam(required = false) String sortOrder) throws ThingsboardException {
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return checkNotNull(otaPackageService.findTenantOtaPackagesByTenantId(getTenantId(), pageLink));
    }

    @ApiOperation(value = "Get OTA Package Infos (getOtaPackages)",
            notes = "Returns a page of OTA Package Info objects owned by tenant. " +
                    PAGE_DATA_PARAMETERS + OTA_PACKAGE_INFO_DESCRIPTION + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/otaPackages/{deviceProfileId}/{type}", method = RequestMethod.GET)
    @ResponseBody
    public PageData<OtaPackageInfo> getOtaPackages(@ApiParam(value = DEVICE_PROFILE_ID_PARAM_DESCRIPTION)
                                                   @PathVariable("deviceProfileId") String strDeviceProfileId,
                                                   @ApiParam(value = "OTA Package type.", allowableValues = "FIRMWARE, SOFTWARE")
                                                   @PathVariable("type") String strType,
                                                   @ApiParam(value = PAGE_SIZE_DESCRIPTION)
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                   @ApiParam(value = PAGE_NUMBER_DESCRIPTION)
                                                   @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                   @ApiParam(value = OTA_PACKAGE_TEXT_SEARCH_DESCRIPTION)
                                                   @RequestParam(required = false) String textSearch,
                                                   @ApiParam(value = SORT_PROPERTY_DESCRIPTION, allowableValues = OTA_PACKAGE_SORT_PROPERTY_ALLOWABLE_VALUES)
                                                   @RequestParam(required = false) String sortProperty,
                                                   @ApiParam(value = SORT_ORDER_DESCRIPTION, allowableValues = SORT_ORDER_ALLOWABLE_VALUES)
                                                   @RequestParam(required = false) String sortOrder) throws ThingsboardException {
        checkParameter("deviceProfileId", strDeviceProfileId);
        checkParameter("type", strType);
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return checkNotNull(otaPackageService.findTenantOtaPackagesByTenantIdAndDeviceProfileIdAndTypeAndHasData(getTenantId(),
                new DeviceProfileId(toUUID(strDeviceProfileId)), OtaPackageType.valueOf(strType), pageLink));
    }

    @ApiOperation(value = "Delete OTA Package (deleteOtaPackage)",
            notes = "Deletes the OTA Package. Referencing non-existing OTA Package Id will cause an error. " +
                    "Can't delete the OTA Package if it is referenced by existing devices or device profile." + TENANT_AUTHORITY_PARAGRAPH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage/{otaPackageId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteOtaPackage(@ApiParam(value = OTA_PACKAGE_ID_PARAM_DESCRIPTION)
                                 @PathVariable("otaPackageId") String strOtaPackageId) throws Exception {
        checkParameter(OTA_PACKAGE_ID, strOtaPackageId);
        OtaPackageId otaPackageId = new OtaPackageId(toUUID(strOtaPackageId));
        OtaPackageInfo otaPackageInfo = checkOtaPackageInfoId(otaPackageId, Operation.DELETE);
        tbOtaPackageService.delete(otaPackageInfo, getCurrentUser());
    }

    @ApiOperation("根据标题和版本号获取OTA")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackage/{title}/{version}", method = RequestMethod.GET)
    @ResponseBody
    public OtaPackageInfo getOtaPackageByTitleVersion(
            @ApiParam("标题")
            @PathVariable("title") String title,
            @ApiParam("版本号")
            @PathVariable("version") String version) throws ThingsboardException {
        return otaPackageService.findByTenantIdAndTitleAndVersion(getTenantId(), title, version);
    }

    @ApiOperation("根据标题获取OTA")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/otaPackageByTitle", method = RequestMethod.GET)
    @ResponseBody
    public OtaPackageInfo getOtaPackageByTitle(
            @ApiParam("标题")
            @RequestParam("title") String title) throws ThingsboardException {
        return otaPackageService.findByTenantIdAndTitle(getTenantId(), title);
    }

}
