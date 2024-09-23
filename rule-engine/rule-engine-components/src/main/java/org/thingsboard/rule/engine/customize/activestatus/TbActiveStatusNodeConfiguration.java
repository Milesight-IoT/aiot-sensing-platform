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

package org.thingsboard.rule.engine.customize.activestatus;

import lombok.Data;
import org.thingsboard.rule.engine.api.NodeConfiguration;

/**
 * 感知对象的通道-节点数据处理
 *
 * @author Luohh
 */
@Data
public class TbActiveStatusNodeConfiguration implements NodeConfiguration<TbActiveStatusNodeConfiguration> {

    /**
     * 设备ID
     */
    private String deviceIds;
    /**
     * 是否显示在部件上
     */
    private boolean showOnWidget;
    /**
     * 规则链ID
     */
    private String ruleChainOwnId;
    /**
     * 规则链名
     */
    private String ruleChainName;

    /**
     * 启动初始化配置方法
     *
     * @return {@link TbActiveStatusNodeConfiguration}
     */
    @Override
    public TbActiveStatusNodeConfiguration defaultConfiguration() {
        TbActiveStatusNodeConfiguration configuration = new TbActiveStatusNodeConfiguration();
        configuration.setShowOnWidget(false);
        return configuration;
    }
}

