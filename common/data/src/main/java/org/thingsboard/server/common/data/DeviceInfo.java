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
package org.thingsboard.server.common.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.id.DeviceId;

@ApiModel
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceInfo extends Device {

    private static final long serialVersionUID = -3004579925090663691L;

    @ApiModelProperty(position = 13, value = "Title of the Customer that owns the device.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String customerTitle;
    @ApiModelProperty(position = 14, value = "Indicates special 'Public' Customer that is auto-generated to use the devices on public dashboards.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private boolean customerIsPublic;
    @ApiModelProperty(position = 15, value = "Name of the corresponding Device Profile.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String deviceProfileName;
    @ApiModelProperty(position = 16, value = "Device active flag.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private boolean active;
    @ApiModelProperty(position = 16, value = "凭证ID")
    private String credentialsId;

    // @ApiModelProperty(position = 17, value = "0 - \"-\";  1 - \"active\";  2 - \"inactive\";")
    // private int active;

    public DeviceInfo() {
        super();
    }

    public DeviceInfo(DeviceId deviceId) {
        super(deviceId);
    }

    public DeviceInfo(Device device, String customerTitle, boolean customerIsPublic, String deviceProfileName, String credentialsId, boolean active) {
        super(device);
        this.customerTitle = customerTitle;
        this.customerIsPublic = customerIsPublic;
        this.deviceProfileName = deviceProfileName;
        this.active = active;
        this.credentialsId = credentialsId;
    }
}
