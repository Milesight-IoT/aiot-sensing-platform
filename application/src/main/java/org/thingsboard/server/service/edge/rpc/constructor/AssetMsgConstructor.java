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
package org.thingsboard.server.service.edge.rpc.constructor;

import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.gen.edge.v1.AssetUpdateMsg;
import org.thingsboard.server.gen.edge.v1.UpdateMsgType;
import org.thingsboard.server.queue.util.TbCoreComponent;

@Component
@TbCoreComponent
public class AssetMsgConstructor {

    public AssetUpdateMsg constructAssetUpdatedMsg(UpdateMsgType msgType, Asset asset) {
        AssetUpdateMsg.Builder builder = AssetUpdateMsg.newBuilder()
                .setMsgType(msgType)
                .setIdMSB(asset.getUuidId().getMostSignificantBits())
                .setIdLSB(asset.getUuidId().getLeastSignificantBits())
                .setName(asset.getName())
                .setType(asset.getType());
        if (asset.getLabel() != null) {
            builder.setLabel(asset.getLabel());
        }
        if (asset.getCustomerId() != null) {
            builder.setCustomerIdMSB(asset.getCustomerId().getId().getMostSignificantBits());
            builder.setCustomerIdLSB(asset.getCustomerId().getId().getLeastSignificantBits());
        }
        if (asset.getAssetProfileId() != null) {
            builder.setAssetProfileIdMSB(asset.getAssetProfileId().getId().getMostSignificantBits());
            builder.setAssetProfileIdLSB(asset.getAssetProfileId().getId().getLeastSignificantBits());
        }
        if (asset.getAdditionalInfo() != null) {
            builder.setAdditionalInfo(JacksonUtil.toString(asset.getAdditionalInfo()));
        }
        return builder.build();
    }

    public AssetUpdateMsg constructAssetDeleteMsg(AssetId assetId) {
        return AssetUpdateMsg.newBuilder()
                .setMsgType(UpdateMsgType.ENTITY_DELETED_RPC_MESSAGE)
                .setIdMSB(assetId.getId().getMostSignificantBits())
                .setIdLSB(assetId.getId().getLeastSignificantBits()).build();
    }
}
