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
package org.thingsboard.server.common.msg.notification.trigger;

import lombok.Builder;
import lombok.Data;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.notification.rule.trigger.NotificationRuleTriggerType;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;

@Data
@Builder
public class RuleEngineComponentLifecycleEventTrigger implements NotificationRuleTrigger {

    private final TenantId tenantId;
    private final RuleChainId ruleChainId;
    private final String ruleChainName;
    private final EntityId componentId;
    private final String componentName;
    private final ComponentLifecycleEvent eventType;
    private final Throwable error;

    @Override
    public NotificationRuleTriggerType getType() {
        return NotificationRuleTriggerType.RULE_ENGINE_COMPONENT_LIFECYCLE_EVENT;
    }

    @Override
    public EntityId getOriginatorEntityId() {
        return componentId;
    }

}
