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
package org.thingsboard.server.dao.sensing;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceAbility;
import org.thingsboard.server.common.data.DeviceAbilityInfo;
import org.thingsboard.server.common.data.SensingObject;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.TelemetryConstants;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.util.CollectionsUtil;
import org.thingsboard.server.common.enume.IEnum;
import org.thingsboard.server.common.enume.device.AbilityTypeEnum;
import org.thingsboard.server.dao.device.DeviceAbilityService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.entity.AbstractCachedEntityService;
import org.thingsboard.server.dao.timeseries.TimeseriesLatestDao;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.DaoUtil.toUUIDsFromString;

/**
 * 感知模块-接口实现
 *
 * @author Luohh
 */
@Service
@Slf4j
public class SensingObjectServiceImpl extends AbstractCachedEntityService<String, SensingObject, SensingObjectEvictEvent> implements SensingObjectService {
    @Autowired
    private SensingObjectDao sensingObjectDao;

    @Autowired
    private DeviceAbilityService deviceAbilityService;
    @Autowired
    private DeviceService deviceService;

    @Resource
    private TimeseriesLatestDao timeseriesLatestDao;

    private final static String IMAGE_ABILITY = "image";

    @TransactionalEventListener(value = SensingObjectEvictEvent.class)
    @Override
    public void handleEvictEvent(SensingObjectEvictEvent event) {
        cache.evict(event.getSensingObjectId());
    }

    @Override
    public PageData<SensingObject> findByTenantId(TenantId tenantId, PageLink pageLink) throws ExecutionException, InterruptedException {
        // 根据参数查询感知对象列表
        PageData<SensingObject> sensingObjectPageData = sensingObjectDao.findByTenantId(tenantId.getId(), pageLink);
        List<SensingObject> data = sensingObjectPageData.getData();
        // 获取该分页设备ID集合
        Set<UUID> deviceAbilityIdSet = new HashSet<>();
        for (SensingObject sensingObject : data) {
            String deviceAbilityIds = sensingObject.getDeviceAbilityIds();
            if (StringUtils.isEmpty(deviceAbilityIds)) {
                continue;
            }
            // 查找感知对象已经关联设备的id
            for (String deviceAbilityId : deviceAbilityIds.split(StringUtils.COMMA)) {
                deviceAbilityIdSet.add(UUID.fromString(deviceAbilityId));
            }
        }
        ArrayList<UUID> deviceAbilityIdList = new ArrayList<>(deviceAbilityIdSet);
        // deviceAbilityIdList 获取 allSensingChannels
        List<DeviceAbilityInfo> allSensingChannels = deviceAbilityService.findByIdIn(deviceAbilityIdList);
        for (DeviceAbilityInfo allSensingChannel : allSensingChannels) {
            String key = allSensingChannel.getAbilityType() == 2 ? TelemetryConstants.IMAGE : allSensingChannel.getAbility();

            TsKvEntry tsKvEntry = timeseriesLatestDao.findLatest(tenantId, new DeviceId(allSensingChannel.getDeviceId()), key).get();
            allSensingChannel.setTs(tsKvEntry.getTs());
            if (allSensingChannel.getAbilityType() == 1) {
                allSensingChannel.setValue(tsKvEntry.getValueAsString());
            }
        }
        for (SensingObject sensingObject : data) {
            List<DeviceAbilityInfo> sensingChannels = new ArrayList<>();
            String deviceAbilityIds = sensingObject.getDeviceAbilityIds();
            if (!StringUtils.isEmpty(deviceAbilityIds)) {
                for (String deviceAbilityId : deviceAbilityIds.split(StringUtils.COMMA)) {
                    Optional<DeviceAbilityInfo> sensingChannel = allSensingChannels.stream()
                            .filter(mapping -> Objects.equals(mapping.getDeviceAbilityId(), UUID.fromString(deviceAbilityId)))
                            .findFirst();
                    sensingChannel.ifPresent(sensingChannels::add);
                }
            }
            sensingObject.setSensingChannels(sensingChannels);
        }

        return sensingObjectPageData;
    }

    @Override
    public TsKvEntry getTsKvEnTry(TenantId tenantId, DeviceId deviceId) {
        TsKvEntry tsKvEnTry;
        try {
            tsKvEnTry = findTsKvEnTry(tenantId, deviceId);
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
        return tsKvEnTry;
    }

    @Override
    public TsKvEntry findTsKvEnTry(TenantId tenantId, DeviceId deviceId) throws ExecutionException, InterruptedException {
        return timeseriesLatestDao.findLatest(tenantId, deviceId, TelemetryConstants.IMAGE).get();
    }

    @Override
    public PageData<SensingObject> findSensingObjectImage(TenantId tenantId, PageLink pageLink, boolean isRoi) throws RuntimeException {
        // 查询设备能力集合字段不为空的感知对象
        PageData<SensingObject> pageData = sensingObjectDao.findBySensingImageAbility(tenantId.getId(), pageLink, isRoi);
        processDataList(tenantId, AbilityTypeEnum.IMAGE.getValue().toString(), pageData.getData(), isRoi);
        return pageData;
    }

    @Override
    public PageData<SensingObject> findSelectListByTenantId(TenantId tenantId, PageLink pageLink, boolean needChannels, String abilityType, boolean isRoi) {
        PageData<SensingObject> pageData = sensingObjectDao.findByTenantId(tenantId.getId(), pageLink);
        List<SensingObject> list = pageData.getData();
        if (!needChannels) {
            return pageData;
        }
        processDataList(tenantId, abilityType, list, isRoi);
        return pageData;
    }

    @Override
    public List<SensingObject> findSensingObjectsByIds(TenantId tenantId, String sensingObjectsIds, boolean needChannels, String abilityType, boolean isRoi) {
        List<UUID> uuids = Arrays.stream(sensingObjectsIds.split(StringUtils.COMMA)).map(UUID::fromString).collect(Collectors.toList());
        List<SensingObject> list = sensingObjectDao.findByIds(uuids);
        if (!needChannels) {
            return list;
        }
        processDataList(tenantId, abilityType, list, isRoi);
        return list;
    }

    private void processDataList(TenantId tenantId, String abilityType, List<SensingObject> list, boolean isRoi) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(sensingObject -> {
            List<DeviceAbilityInfo> sensingChannels = new ArrayList<>();
            String deviceAbilityIds = sensingObject.getDeviceAbilityIds();
            if (StringUtils.isBlank(deviceAbilityIds)) {
                return;
            }

            Arrays.stream(deviceAbilityIds.split(StringUtils.COMMA))
                    .filter(StringUtils::isNotBlank)
                    .forEach(abilityId -> {
                        DeviceAbility ability = deviceAbilityService.getDeviceAbilityByIdCache(UUID.fromString(abilityId));
                        if (ability == null) {
                            return;
                        }
                        AbilityTypeEnum abilityTypeEnum = IEnum.of(AbilityTypeEnum.class, ability.getAbilityType());
                        if (StringUtils.isNotBlank(abilityType) && !abilityTypeEnum.eq(Short.parseShort(abilityType))) {
                            return;
                        }
                        if (isRoi && ability.getAbility().equals(IMAGE_ABILITY)) {
                            return;
                        }
                        DeviceAbilityInfo deviceAbilityInfo = new DeviceAbilityInfo(ability);
                        UUID deviceId = ability.getDeviceId();
                        Device deviceById = deviceService.findDeviceById(tenantId, new DeviceId(deviceId));
                        if (deviceById != null) {
                            deviceAbilityInfo.setDeviceName(deviceById.getName());
                        }
                        sensingChannels.add(deviceAbilityInfo);
                    });

            sensingObject.setSensingChannels(sensingChannels);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SensingObject saveSensingObject(TenantId tenantId, SensingObject sensingObject) throws Exception {
        // 更新获取就得设备能力id列表
        List<String> oldDeviceAbilityIdList = getOldDeviceAbilityIdList(tenantId, sensingObject);
        // 保存新的感知对象
        sensingObject.setTenantId(tenantId.getId());
        try {
            SensingObject newSensingObject = sensingObjectDao.save(tenantId, sensingObject);
            // 新的感知对象deviceAbilityIds关联ID
            String deviceAbilityIds = newSensingObject.getDeviceAbilityIds();
            List<String> deviceAbilityIdList = StringUtils.isEmpty(deviceAbilityIds)
                    ? new ArrayList<>() : Arrays.asList(deviceAbilityIds.split(StringUtils.COMMA));

            // 处理关联表DeviceAbility数据
            processDeviceAbility(newSensingObject, oldDeviceAbilityIdList, deviceAbilityIdList);

            // 计算 sensingChannels
            List<DeviceAbilityInfo> sensingChannels = new ArrayList<>();
            for (String deviceAbilityId : deviceAbilityIdList) {
                DeviceAbilityInfo deviceAbilityInfo = deviceAbilityService.getDeviceAbilityInfoById(UUID.fromString(deviceAbilityId));
                String key = (deviceAbilityInfo.getAbilityType() == 2) ? TelemetryConstants.IMAGE : deviceAbilityInfo.getAbility();
                TsKvEntry tsKvEntry = timeseriesLatestDao.findLatest(tenantId, new DeviceId(deviceAbilityInfo.getDeviceId()), key).get();
                deviceAbilityInfo.setTs(tsKvEntry.getTs());
                if (deviceAbilityInfo.getAbilityType() == 1) {
                    deviceAbilityInfo.setValue(tsKvEntry.getValueAsString());
                }
                sensingChannels.add(deviceAbilityInfo);
                log.info("save sensing object  name = {} ,association ability ability = {}", sensingObject.getName(), deviceAbilityInfo.getAbility());
            }
            newSensingObject.setSensingChannels(sensingChannels);
            // 驱逐缓存
            publishEvictEvent(new SensingObjectEvictEvent(newSensingObject.getId().toString()));
            log.info("save sensing object name {} , id = {}", newSensingObject.getName(), newSensingObject.getId());
            return newSensingObject;
        } catch (Exception e) {
            // 异常清除缓存
            handleEvictEvent(new SensingObjectEvictEvent(sensingObject.getId().toString()));
            throw e;
        }
    }

    private void processDeviceAbility(SensingObject newSensingObject, List<String> oldDeviceAbilityIdList,
                                      List<String> deviceAbilityIdList) throws ThingsboardException {

        // 去重（已删除 + 新增）查询设备能力id列表
        Set<String> allDeviceAbilityIdList = new HashSet<>(oldDeviceAbilityIdList);
        allDeviceAbilityIdList.addAll(deviceAbilityIdList);
        List<DeviceAbility> deviceAbilities
                = deviceAbilityService.findAllById(toUUIDsFromString(new ArrayList<>(allDeviceAbilityIdList)));
        UUID sensingObjectId = newSensingObject.getId();

        List<String> alreadyAssociatedAbilityList = new ArrayList<>();
        deviceAbilities.forEach(ability -> {
            String deviceAbilityId = ability.getId().toString();
            UUID newSensingObjectId = newSensingObject.getId();
            // 二次校验设备能力和通道关系（一对一）
            UUID deviceAbilitySensingObjectId = ability.getSensingObjectId();

            if (deviceAbilitySensingObjectId != null
                    && deviceAbilityIdList.contains(deviceAbilityId)
                    && !sensingObjectId.equals(deviceAbilitySensingObjectId)) {
                // 已经被其他关联的能力
                alreadyAssociatedAbilityList.add(ability.getAbility());
                return;
            }

            if (oldDeviceAbilityIdList.contains(deviceAbilityId)) {
                // 解除设备能力关联感知对象
                ability.setSensingObjectId(null);
            }

            if (deviceAbilityIdList.contains(deviceAbilityId)) {
                // 建立设备能力关联感知对象
                ability.setSensingObjectId(newSensingObjectId);
            }
        });
        if (CollectionsUtil.isNotEmpty(alreadyAssociatedAbilityList)) {
            String message = "Device Capabilities (" + String.join(StringUtils.COMMA, alreadyAssociatedAbilityList) + ") Already Associated";
            throw new ThingsboardException(message, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        // 批量保存
        deviceAbilityService.saveAll(deviceAbilities);
    }

    @NotNull
    private List<String> getOldDeviceAbilityIdList(TenantId tenantId, SensingObject sensingObject) {
        List<String> oldDeviceAbilityIdList = new ArrayList<>();
        if (sensingObject.getId() == null) {
            return oldDeviceAbilityIdList;
        }
        SensingObject oldSensingObject = sensingObjectDao.findById(tenantId, sensingObject.getId());
        String deviceAbilityIds = oldSensingObject.getDeviceAbilityIds();
        if (!StringUtils.isEmpty(deviceAbilityIds)) {
            log.info("Sensing object associate deviceAbilityIds: {}", deviceAbilityIds);
            oldDeviceAbilityIdList = Arrays.asList(deviceAbilityIds.split(StringUtils.COMMA));
        }
        return oldDeviceAbilityIdList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSensingObject(UUID sensingObjectId) throws ThingsboardException {
        SensingObject sensingObjectById = getSensingObjectById(sensingObjectId);
        if (sensingObjectById == null) {
            log.error("id = {} not found db object", sensingObjectId);
            throw new ThingsboardException("data not found , does not exist!", ThingsboardErrorCode.GENERAL);
        }
        log.info("remove sensing object name = {} , id = {}", sensingObjectById.getName(), sensingObjectId);
        // 更新关联关系
        deviceAbilityService.updateSensingObjectIdBySensingObjectId(sensingObjectId);
        // 删除
        sensingObjectDao.removeById(null, sensingObjectId);
        // 清理缓存
        handleEvictEvent(new SensingObjectEvictEvent(sensingObjectId.toString()));
    }

    @Override
    public List<SensingObject> findByDeviceId(UUID deviceId) {
        return sensingObjectDao.findByDeviceId(deviceId);
    }

    @Override
    public SensingObject findByDeviceAbilityId(UUID deviceAbilityId) {
        return sensingObjectDao.findByDeviceAbilityId(deviceAbilityId);
    }

    @Override
    public SensingObject findByTenantIdAndName(TenantId tenantId, String name) {
        return sensingObjectDao.findByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public List<SensingObject> findCacheByIds(List<String> sensingObjectIds) {
        if (CollectionUtils.isEmpty(sensingObjectIds)) {
            log.debug("findByIds is empty!");
            return Collections.emptyList();
        }
        List<UUID> uuids = toUUIDsFromString(sensingObjectIds);
        return uuids.stream().map(this::getSensingObjectById).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public SensingObject getSensingObjectById(UUID uuid) {
        return cache.getAndPutInTransaction(uuid.toString(),
                () -> sensingObjectDao.findById(null, uuid),
                true);
    }

    @Override
    public List<String> findAbilityIdsBySensingObjectIds(List<String> sensingObjectId) {
        List<SensingObject> cacheByIds = findCacheByIds(sensingObjectId);
        if (CollectionUtils.isEmpty(cacheByIds)) {
            log.debug("findAbilityIdsByIds is empty");
            return Collections.emptyList();
        }
        Set<String> deviceAbilityIdSet = new HashSet<>();
        cacheByIds.stream()
                .filter(obj -> !obj.getDeviceAbilityIds().isEmpty())
                .forEach(sensingObject -> {
                    String deviceAbilityIds = sensingObject.getDeviceAbilityIds();
                    Set<String> collect = Arrays.stream(deviceAbilityIds.split(StringUtils.COMMA)).collect(Collectors.toSet());
                    deviceAbilityIdSet.addAll(collect);
                });
        return new ArrayList<>(deviceAbilityIdSet);
    }
}
