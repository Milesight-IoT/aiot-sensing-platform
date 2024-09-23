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
package org.thingsboard.server.dao.sql.device;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.DeviceAbilityInfo;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.device.RoiChildSelectData;
import org.thingsboard.server.common.data.device.RoiSelectData;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.UUIDBased;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.device.DeviceAbilityDao;
import org.thingsboard.server.dao.model.sql.DeviceAbilityEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JpaDeviceAbilityDao extends JpaAbstractDao<DeviceAbilityEntity, DeviceAbility> implements DeviceAbilityDao {
    @Autowired
    private DeviceAbilityRepository deviceAbilityRepository;
    @Autowired
    private EntityManager manager;

    @Override
    protected Class<DeviceAbilityEntity> getEntityClass() {
        return DeviceAbilityEntity.class;
    }

    @Override
    protected JpaRepository<DeviceAbilityEntity, UUID> getRepository() {
        return deviceAbilityRepository;
    }

    @Override
    public PageData<DeviceAbility> findDeviceAbilitiesByDeviceId(UUID deviceId, PageLink pageLink) {
        return DaoUtil.toPageData(
                deviceAbilityRepository.findDeviceAbilitiesByDeviceId(
                        deviceId, DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<DeviceAbility> findDeviceAbilitiesByDeviceIdAndAbilityType(UUID deviceId, short abilityType, PageLink pageLink) {
        return DaoUtil.toPageData(
                deviceAbilityRepository.findDeviceAbilitiesByDeviceIdAndAbilityType(
                        deviceId, abilityType, DaoUtil.toPageable(pageLink)));
    }

    @Override
    public int updateSensingObjectIdBySensingObjectId(UUID sensingObjectId) {
        return deviceAbilityRepository.updateSensingObjectIdBySensingObjectId(sensingObjectId);
    }

    @Override
    public List<DeviceAbilityInfo> findByIdIn(List<UUID> deviceAbilityIdList) {
        return DaoUtil.convertDataList(deviceAbilityRepository.findByIdIn(deviceAbilityIdList));
    }

    @Override
    public List<DeviceAbility> findAllById(List<UUID> deviceAbilityIdList) {
        return DaoUtil.convertDataList(deviceAbilityRepository.findAllById(deviceAbilityIdList));
    }

    @Override
    public DeviceAbilityInfo findDeviceAbilityInfoById(UUID deviceAbilityId) {
        return DaoUtil.getData(deviceAbilityRepository.findDeviceAbilityInfoById(deviceAbilityId));
    }

    @Override
    public DeviceAbility findDeviceAbilityByDeviceIdAndAbility(UUID deviceId, String ability) {
        return DaoUtil.getData(deviceAbilityRepository.findDeviceAbilityByDeviceIdAndAbility(deviceId, ability));
    }

    @Override
    public List<DeviceAbility> findAllBySensingObjectId(UUID sensingObjectId) {
        return DaoUtil.convertDataList(deviceAbilityRepository.findAllBySensingObjectId(sensingObjectId));
    }

    @Override
    public List<DeviceAbility> findImageByDeviceIds(List<DeviceId> deviceIdList) {
        List<UUID> uuidList = deviceIdList.stream().map(UUIDBased::getId).collect(Collectors.toList());
        return DaoUtil.convertDataList(deviceAbilityRepository.findImageByDeviceIds(uuidList));
    }

    @Override
    public PageData<RoiSelectData> findRoiSelectList(UUID tenantId, PageLink pageLink, boolean isRoi, Short abilityType) {
        Page<Map<String, String>> page = deviceAbilityRepository.findRoiSelectList(tenantId, abilityType, isRoi,
                pageLink.getTextSearch(),
                DaoUtil.toPageable(pageLink));
        List<RoiSelectData> list = convertRoiSelectData(page.toList());
        return new PageData<>(list, page.getTotalPages(), page.getTotalElements(), page.hasNext());
    }

    private List<RoiSelectData> convertRoiSelectData(List<Map<String, String>> list) {
        return list.stream()
                .map(map -> {
                    RoiSelectData roiSelectData = new RoiSelectData();
                    // 驼峰被消除deviceId -> deviceid
                    roiSelectData.setDeviceId(MapUtils.getString(map, "deviceid", ""));
                    roiSelectData.setDeviceName(MapUtils.getString(map, "devicename", ""));

                    String abilityIdList = MapUtils.getString(map, "abilityidlist", "");
                    if (StringUtils.isNotBlank(abilityIdList)) {
                        String abilityList = MapUtils.getString(map, "abilitylist", "");
                        String[] abilitySplit = abilityList.split(StringUtils.COMMA);

                        List<RoiChildSelectData> childSelectData = roiSelectData.getChildSelectData();
                        String[] abilityIdSplit = abilityIdList.split(StringUtils.COMMA);
                        for (int i = 0; i < abilityIdSplit.length; i++) {
                            RoiChildSelectData roiChildSelectData = new RoiChildSelectData(abilitySplit[i], abilityIdSplit[i]);
                            childSelectData.add(roiChildSelectData);
                        }
                    }
                    return roiSelectData;
                }).collect(Collectors.toList());
    }

    @Override
    public List<RoiSelectData> findRoiSelectByDeviceList(UUID tenantId, List<UUID> deviceIdList, boolean isRoi, Short abilityType) {
        List<Map<String, String>> list = deviceAbilityRepository.findRoiSelectByDeviceList(tenantId, abilityType, isRoi, deviceIdList);
        return convertRoiSelectData(list);
    }

    @Override
    public List<DeviceAbility> findByDeviceId(UUID deviceId) {
        return DaoUtil.convertDataList(deviceAbilityRepository.findByDeviceId(deviceId));
    }

}
