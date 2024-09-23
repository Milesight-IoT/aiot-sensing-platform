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
package org.thingsboard.server.service.install;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.queue.settings.TbRuleEngineQueueConfiguration;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Data
@EnableAutoConfiguration
@Configuration
@ConfigurationProperties(prefix = "queue.rule-engine")
@Profile("install")
public class TbRuleEngineQueueConfigService {

    private String topic;
    private List<TbRuleEngineQueueConfiguration> queues;

    @PostConstruct
    public void validate() {
        queues.stream().filter(queue -> queue.getName().equals(DataConstants.MAIN_QUEUE_NAME)).findFirst().orElseThrow(() -> {
            log.error("Main queue is not configured in msaiotsensingplatform.yml");
            return new RuntimeException("No \"Main\" queue configured!");
        });
    }

}
