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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.thingsboard.server.common.data.id.DeviceId;

import java.io.Serializable;
import java.util.UUID;

/**
 * 设备能力缓存
 *
 * @author Luohh
 */
@Data
@AllArgsConstructor
public class DeviceAbilityCacheKey implements Serializable {

    private static final long serialVersionUID = 8220455917177676472L;

    private final String ability;
    private final DeviceId deviceId;
    private final UUID deviceAbilityId;

    public static DeviceAbilityCacheKey fromDeviceIdAbility(DeviceId deviceId, String ability) {
        return new DeviceAbilityCacheKey(ability, deviceId, null);
    }

    public static DeviceAbilityCacheKey fromId(UUID deviceAbilityId) {
        return new DeviceAbilityCacheKey(null, null, deviceAbilityId);
    }

    @Override
    public String toString() {
        if (deviceAbilityId != null) {
            return deviceAbilityId.toString();
        } else {
            return deviceId.getId().toString() + "_" + ability;
        }
    }
}
