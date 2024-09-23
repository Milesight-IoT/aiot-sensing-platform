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
package org.thingsboard.server.service.entitiy.ota;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceInfo;
import org.thingsboard.server.common.data.DeviceInfoFilter;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.OtaPackage;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.SaveOtaPackageInfoRequest;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.TelemetryConstants;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.ota.ChecksumAlgorithm;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.ota.OtaPackageService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.AbstractTbEntityService;
import org.thingsboard.server.service.entitiy.device.profile.TbDeviceProfileService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@TbCoreComponent
@AllArgsConstructor
@Slf4j
public class DefaultTbOtaPackageService extends AbstractTbEntityService implements TbOtaPackageService {

    private final TbDeviceProfileService tbDeviceProfileService;

    private final OtaPackageService otaPackageService;

    private final DeviceProfileService deviceProfileService;

    private final DeviceService deviceService;

    private final AttributesService attributesService;

    @Override
    public OtaPackageInfo save(SaveOtaPackageInfoRequest saveOtaPackageInfoRequest, User user) throws ThingsboardException {
        ActionType actionType = saveOtaPackageInfoRequest.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        TenantId tenantId = saveOtaPackageInfoRequest.getTenantId();
        try {
            OtaPackageInfo savedOtaPackageInfo = otaPackageService.saveOtaPackageInfo(new OtaPackageInfo(saveOtaPackageInfoRequest), saveOtaPackageInfoRequest.isUsesUrl());

            boolean sendMsgToEdge = savedOtaPackageInfo.hasUrl() || savedOtaPackageInfo.isHasData();
            notificationEntityService.notifyCreateOrUpdateOrDelete(tenantId, null, savedOtaPackageInfo.getId(),
                    savedOtaPackageInfo, user, actionType, sendMsgToEdge, null);

            return savedOtaPackageInfo;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.OTA_PACKAGE), saveOtaPackageInfoRequest,
                    actionType, user, e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OtaPackageInfo saveOtaPackageData(OtaPackageInfo otaPackageInfo, String checksum, ChecksumAlgorithm checksumAlgorithm,
                                             byte[] data, String filename, String contentType, User user) throws Exception {
        TenantId tenantId = otaPackageInfo.getTenantId();
        OtaPackageId otaPackageId = otaPackageInfo.getId();
        try {
            if (StringUtils.isEmpty(checksum)) {
                checksum = otaPackageService.generateChecksum(checksumAlgorithm, ByteBuffer.wrap(data));
            }
            OtaPackage otaPackage = new OtaPackage(otaPackageId);
            otaPackage.setCreatedTime(otaPackageInfo.getCreatedTime());
            otaPackage.setTenantId(tenantId);
            otaPackage.setDeviceProfileId(otaPackageInfo.getDeviceProfileId());
            otaPackage.setType(otaPackageInfo.getType());
            otaPackage.setTitle(otaPackageInfo.getTitle());
            otaPackage.setVersion(otaPackageInfo.getVersion());
            otaPackage.setTag(otaPackageInfo.getTag());
            otaPackage.setAdditionalInfo(otaPackageInfo.getAdditionalInfo());
            otaPackage.setDistributeStatus(otaPackageInfo.getDistributeStatus());
            otaPackage.setChecksumAlgorithm(checksumAlgorithm);
            otaPackage.setChecksum(checksum);
            otaPackage.setFileName(filename);
            otaPackage.setContentType(contentType);
            otaPackage.setData(ByteBuffer.wrap(data));
            otaPackage.setDataSize((long) data.length);
            OtaPackageInfo savedOtaPackage = otaPackageService.saveOtaPackage(otaPackage);
            notificationEntityService.notifyCreateOrUpdateOrDelete(tenantId, null, savedOtaPackage.getId(),
                    savedOtaPackage, user, ActionType.UPDATED, true, null);

            if (savedOtaPackage.getDistributeStatus()) {
                // 更新设备型号关联的OTA
                deviceProfileService.updateDeviceProfileOtaId(
                        savedOtaPackage.getDeviceProfileId().getId(), savedOtaPackage.getType(), savedOtaPackage.getId().getId());

                // 更新设备属性
                List<String> attributeKeys = null;
                String title = null;
                String checksumField = null;
                if (savedOtaPackage.getType().equals(OtaPackageType.FIRMWARE)) {
                    attributeKeys = TelemetryConstants.FW_ATTRIBUTE_KEYS;
                    title = TelemetryConstants.FW_TITLE;
                    checksumField = TelemetryConstants.FW_CHECKSUM;
                }
                if (savedOtaPackage.getType().equals(OtaPackageType.CONFIGURE)) {
                    attributeKeys = TelemetryConstants.CF_ATTRIBUTE_KEYS;
                    title = TelemetryConstants.CF_TITLE;
                    checksumField = TelemetryConstants.CF_CHECKSUM;
                }
                DeviceInfoFilter deviceInfoFilter
                        = DeviceInfoFilter.builder().deviceProfileId(otaPackageInfo.getDeviceProfileId()).tenantId(tenantId).build();
                List<DeviceInfo> deviceInfos = deviceService.findDeviceInfosByFilter(deviceInfoFilter, new PageLink(999)).getData();
                for (DeviceInfo deviceInfo : deviceInfos) {
                    attributesService.removeAll(tenantId, deviceInfo.getId(), DataConstants.SHARED_SCOPE, attributeKeys).get();

                    AttributeKvEntry attrTitle = new BaseAttributeKvEntry(new StringDataEntry(title, savedOtaPackage.getTitle()), savedOtaPackage.getCreatedTime());
                    AttributeKvEntry attrChecksum = new BaseAttributeKvEntry(new StringDataEntry(checksumField, savedOtaPackage.getChecksum()), savedOtaPackage.getCreatedTime());
                    List<AttributeKvEntry> attrList = new ArrayList<>();
                    attrList.add(attrTitle);
                    attrList.add(attrChecksum);
                    attributesService.save(tenantId, deviceInfo.getId(), DataConstants.SHARED_SCOPE, attrList).get();
                }
            }

            return savedOtaPackage;
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.OTA_PACKAGE), ActionType.UPDATED,
                    user, e, otaPackageId.toString());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(OtaPackageInfo otaPackageInfo, User user) throws Exception {
        TenantId tenantId = otaPackageInfo.getTenantId();
        OtaPackageId otaPackageId = otaPackageInfo.getId();
        try {
            List<String> attributeKeys = null;
            if (otaPackageInfo.getType().equals(OtaPackageType.FIRMWARE)) {
                attributeKeys = TelemetryConstants.FW_ATTRIBUTE_KEYS;
            }
            if (otaPackageInfo.getType().equals(OtaPackageType.CONFIGURE)) {
                attributeKeys = TelemetryConstants.CF_ATTRIBUTE_KEYS;
            }
            DeviceInfoFilter deviceInfoFilter
                    = DeviceInfoFilter.builder().deviceProfileId(otaPackageInfo.getDeviceProfileId()).tenantId(tenantId).build();
            List<DeviceInfo> deviceInfos = deviceService.findDeviceInfosByFilter(deviceInfoFilter, new PageLink(999)).getData();
//            List<DeviceId> processedDeviceIds = new ArrayList<>();
            for (DeviceInfo deviceInfo : deviceInfos) {
                Device device = new Device(deviceInfo);
                if (otaPackageInfo.getType().equals(OtaPackageType.FIRMWARE) && Objects.equals(otaPackageInfo.getId(), device.getFirmwareId())) {
                    device.setFirmwareId(null);
                    deviceService.save(device);
//                    deleteAttribute(tenantId, device, attributeKeys, otaPackageInfo);
//                    processedDeviceIds.add(device.getId());
                }
                if (otaPackageInfo.getType().equals(OtaPackageType.SOFTWARE) && Objects.equals(otaPackageInfo.getId(), device.getSoftwareId())) {
                    device.setSoftwareId(null);
                    deviceService.save(device);
//                    deleteAttribute(tenantId, device, attributeKeys, otaPackageInfo);
//                    processedDeviceIds.add(device.getId());
                }
                if (otaPackageInfo.getType().equals(OtaPackageType.CONFIGURE) && Objects.equals(otaPackageInfo.getId(), device.getConfigureId())) {
                    device.setConfigureId(null);
                    deviceService.save(device);
//                    deleteAttribute(tenantId, device, attributeKeys, otaPackageInfo);
//                    processedDeviceIds.add(device.getId());
                }
            }
            DeviceProfile deviceProfile = deviceProfileService.findDeviceProfileById(tenantId, otaPackageInfo.getDeviceProfileId());
            if (Objects.equals(otaPackageInfo.getId(), deviceProfile.getFirmwareId()) ||
                    Objects.equals(otaPackageInfo.getId(), deviceProfile.getSoftwareId()) ||
                    Objects.equals(otaPackageInfo.getId(), deviceProfile.getConfigureId())) {
                deviceProfileService.updateDeviceProfileOtaId(
                        otaPackageInfo.getDeviceProfileId().getId(), otaPackageInfo.getType(), null);

                for (DeviceInfo deviceInfo : deviceInfos) {
                    Device device = new Device(deviceInfo);
//                    if (processedDeviceIds.contains(device.getId())) {
//                        continue;
//                    }
                    deleteAttribute(tenantId, device, attributeKeys, otaPackageInfo);
                }
            }

            otaPackageService.deleteOtaPackage(tenantId, otaPackageId);
//            notificationEntityService.notifyCreateOrUpdateOrDelete(tenantId, null, otaPackageId, otaPackageInfo,
//                    user, ActionType.DELETED, true, null, otaPackageInfo.getId().toString());
        } catch (Exception e) {
            notificationEntityService.logEntityAction(tenantId, emptyId(EntityType.OTA_PACKAGE),
                    ActionType.DELETED, user, e, otaPackageId.toString());
            throw e;
        }
    }

    public void deleteAttribute(TenantId tenantId, Device device, List<String> attributeKeys, OtaPackageInfo otaPackageInfo) throws ExecutionException, InterruptedException {
        List<AttributeKvEntry> attributeKvEntries = attributesService.find(tenantId, device.getId(), DataConstants.SHARED_SCOPE, attributeKeys).get();
        if (attributeKvEntries.size() >= 2 && attributeKvEntries.get(0).getValueAsString().equals(otaPackageInfo.getTitle()) &&
                attributeKvEntries.get(1).getValueAsString().equals(otaPackageInfo.getVersion())) {
            attributesService.removeAll(tenantId, device.getId(), DataConstants.SHARED_SCOPE, attributeKeys).get();
        }
    }
}
