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
package org.thingsboard.rule.engine.metadata;

import lombok.Data;
import org.thingsboard.rule.engine.api.NodeConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * Created by ashvayka on 19.01.18.
 */
@Data
public class TbGetAttributesNodeConfiguration implements NodeConfiguration<TbGetAttributesNodeConfiguration> {

    private List<String> clientAttributeNames;
    private List<String> sharedAttributeNames;
    private List<String> serverAttributeNames;

    private List<String> latestTsKeyNames;

    private boolean tellFailureIfAbsent;
    private boolean getLatestValueWithTs;
    private boolean fetchToData;

    @Override
    public TbGetAttributesNodeConfiguration defaultConfiguration() {
        TbGetAttributesNodeConfiguration configuration = new TbGetAttributesNodeConfiguration();
        configuration.setClientAttributeNames(Collections.emptyList());
        configuration.setSharedAttributeNames(Collections.emptyList());
        configuration.setServerAttributeNames(Collections.emptyList());
        configuration.setLatestTsKeyNames(Collections.emptyList());
        configuration.setTellFailureIfAbsent(true);
        configuration.setGetLatestValueWithTs(false);
        configuration.setFetchToData(false);
        return configuration;
    }
}
