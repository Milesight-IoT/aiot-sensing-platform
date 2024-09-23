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
package org.thingsboard.server.dao.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.DeviceAbilityInfo;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.device.RoiSelectData;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.entity.AbstractCachedEntityService;
import org.thingsboard.server.dao.rule.RuleChainAssociateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.DaoUtil.toUUIDsFromString;

/**
 * 设备能力 - 接口实现
 *
 * @author xu.yanchuan
 */
@Slf4j
@Service("DeviceAbilityDaoService")
public class DeviceAbilityServiceImpl
        extends AbstractCachedEntityService<DeviceAbilityCacheKey, DeviceAbility, DeviceAbilityEvictEvent>
        implements DeviceAbilityService {
    @Autowired
    private DeviceAbilityDao deviceAbilityDao;
    @Autowired
    private RuleChainAssociateService ruleChainAssociateService;

    @Override
    @TransactionalEventListener(classes = DeviceAbilityEvictEvent.class)
    public void handleEvictEvent(DeviceAbilityEvictEvent event) {
        List<DeviceAbilityCacheKey> keys = new ArrayList<>(2);
        if (event.getDeviceAbilityId() != null) {
            keys.add(DeviceAbilityCacheKey.fromId(event.getDeviceAbilityId()));
        }
        if (event.getDeviceId() != null && event.getAbility() != null) {
            DeviceAbilityCacheKey key = DeviceAbilityCacheKey.fromDeviceIdAbility(event.getDeviceId(), event.getAbility());
            keys.add(key);
        }
        cache.evict(keys);
    }

    @Override
    public PageData<DeviceAbility> findDeviceAbilitiesByDeviceIdAndAbilityType(UUID deviceId, short abilityType, PageLink pageLink) {
        if (abilityType == 0) {
            return deviceAbilityDao.findDeviceAbilitiesByDeviceId(deviceId, pageLink);
        }
        return deviceAbilityDao.findDeviceAbilitiesByDeviceIdAndAbilityType(deviceId, abilityType, pageLink);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceAbility saveDeviceAbilityAndCache(DeviceAbility deviceAbility) {
        try {
            DeviceAbility save = deviceAbilityDao.saveAndFlush(null, deviceAbility);
            publishEvictEvent(save);
            log.info("createOrUpdate DeviceAbility name = {} , id = {}", save.getAbility(), save.getId());
            return save;
        } catch (Exception e) {
            handleEvictEvent(new DeviceAbilityEvictEvent(deviceAbility.getAbility(), new DeviceId(deviceAbility.getDeviceId()),
                    deviceAbility.getId()));
            throw e;
        }
    }

    private void publishEvictEvent(DeviceAbility save) {
        // 保存数据后驱逐
        publishEvictEvent(new DeviceAbilityEvictEvent(save.getAbility(), new DeviceId(save.getDeviceId()), save.getId()));
    }

    @Override
    public PageData<RoiSelectData> findRoiSelectList(TenantId tenantId, Short abilityType, PageLink pageLink, boolean isRoi) {
        PageData<RoiSelectData> pageData = deviceAbilityDao.findRoiSelectList(tenantId.getId(), pageLink, isRoi, abilityType);
        return pageData;
    }

    @Override
    public List<RoiSelectData> findRoiSelectByDeviceList(TenantId tenantId, String deviceIds, Short abilityType, boolean isRoi) throws ThingsboardException {
        if (StringUtils.isBlank(deviceIds)) {
            throw new ThingsboardException("Invalid deviceIds can not be empty !", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        List<String> deviceIdList = Arrays.asList(deviceIds.split(StringUtils.COMMA));
        List<UUID> uuids = toUUIDsFromString(deviceIdList);
        return deviceAbilityDao.findRoiSelectByDeviceList(tenantId.getId(), uuids, isRoi, abilityType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceAbility(UUID deviceAbilityId) {
        DeviceAbility deviceAbility = deviceAbilityDao.findById(null, deviceAbilityId);
        removeDeviceAbility(deviceAbility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDeviceId(UUID deviceId) {
        List<DeviceAbility> byDeviceId = deviceAbilityDao.findByDeviceId(deviceId);
        for (DeviceAbility deviceAbility : byDeviceId) {
            removeDeviceAbility(deviceAbility);
        }
    }

    private void removeDeviceAbility(DeviceAbility deviceAbility) {
        UUID id = deviceAbility.getId();
        deviceAbilityDao.removeById(null, id);
        // 删除规则链关系
        ruleChainAssociateService.deleteByAssociateId(id);
        handleEvictEvent(new DeviceAbilityEvictEvent(deviceAbility.getAbility(), new DeviceId(deviceAbility.getDeviceId()), deviceAbility.getId()));
        log.info("remove DeviceAbility name = {} , id = {}", deviceAbility.getAbility(), deviceAbility.getId());
    }

    @Override
    public DeviceAbility findDeviceAbilityByDeviceIdAndAbility(UUID deviceId, String ability) {
        return deviceAbilityDao.findDeviceAbilityByDeviceIdAndAbility(deviceId, ability);
    }

    @Override
    public List<DeviceAbility> findCacheByIds(List<String> abilityIds) {
        if (CollectionUtils.isEmpty(abilityIds)) {
            log.debug("findByIds is empty");
            return Collections.emptyList();
        }
        return toUUIDsFromString(abilityIds).stream().map(this::getDeviceAbilityByIdCache).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public DeviceAbility getDeviceAbilityByIdCache(UUID abilityId) {
        DeviceAbilityCacheKey deviceAbilityCacheKey = DeviceAbilityCacheKey.fromId(abilityId);
        return cache.getAndPutInTransaction(deviceAbilityCacheKey,
                () -> deviceAbilityDao.findById(null, abilityId), true);
    }

    @Override
    public void saveAllSync(List<DeviceAbility> newDeviceAbilities) {
        deviceAbilityDao.saveAll(newDeviceAbilities);
    }

    @Override
    public DeviceAbilityInfo getDeviceAbilityInfoById(UUID uuid) {
        return deviceAbilityDao.findDeviceAbilityInfoById(uuid);
    }

    @Override
    public List<DeviceAbility> findByDeviceId(UUID deviceId) {
        return deviceAbilityDao.findByDeviceId(deviceId);
    }


    @Override
    public void updateSensingObjectIdBySensingObjectId(UUID sensingObjectId) {
        List<DeviceAbility> deviceAbilities = deviceAbilityDao.findAllBySensingObjectId(sensingObjectId);
        deviceAbilities.forEach(deviceAbility -> {
            deviceAbility.setSensingObjectId(null);
            saveDeviceAbilityAndCache(deviceAbility);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<DeviceAbility> deviceAbilities) {
        if (CollectionUtils.isEmpty(deviceAbilities)) {
            log.debug("No device abilities");
            return;
        }
        deviceAbilityDao.saveAll(deviceAbilities);
    }

    @Override
    public List<DeviceAbility> findAllById(List<UUID> uuids) {
        return deviceAbilityDao.findAllById(uuids);
    }

    @Override
    public List<DeviceAbilityInfo> findByIdIn(ArrayList<UUID> uuids) {
        return deviceAbilityDao.findByIdIn(uuids);
    }

}
