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
package org.thingsboard.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.common.data.id.NotificationRuleId;
import org.thingsboard.server.common.data.id.NotificationTemplateId;
import org.thingsboard.server.common.data.notification.rule.NotificationRule;
import org.thingsboard.server.common.data.notification.rule.NotificationRuleConfig;
import org.thingsboard.server.common.data.notification.rule.NotificationRuleRecipientsConfig;
import org.thingsboard.server.common.data.notification.rule.trigger.NotificationRuleTriggerConfig;
import org.thingsboard.server.common.data.notification.rule.trigger.NotificationRuleTriggerType;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.UUID;

@Data @EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.NOTIFICATION_RULE_TABLE_NAME)
public class NotificationRuleEntity extends BaseSqlEntity<NotificationRule> {

    @Column(name = ModelConstants.TENANT_ID_PROPERTY, nullable = false)
    private UUID tenantId;

    @Column(name = ModelConstants.NAME_PROPERTY, nullable = false)
    private String name;

    @Column(name = ModelConstants.NOTIFICATION_RULE_TEMPLATE_ID_PROPERTY, nullable = false)
    private UUID templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.NOTIFICATION_RULE_TRIGGER_TYPE_PROPERTY, nullable = false)
    private NotificationRuleTriggerType triggerType;

    @Type(type = "json")
    @Column(name = ModelConstants.NOTIFICATION_RULE_TRIGGER_CONFIG_PROPERTY, nullable = false)
    private JsonNode triggerConfig;

    @Type(type = "json")
    @Column(name = ModelConstants.NOTIFICATION_RULE_RECIPIENTS_CONFIG_PROPERTY, nullable = false)
    private JsonNode recipientsConfig;

    @Type(type = "json")
    @Column(name = ModelConstants.NOTIFICATION_RULE_ADDITIONAL_CONFIG_PROPERTY)
    private JsonNode additionalConfig;

    public NotificationRuleEntity() {}

    public NotificationRuleEntity(NotificationRule notificationRule) {
        setId(notificationRule.getUuidId());
        setCreatedTime(notificationRule.getCreatedTime());
        setTenantId(getTenantUuid(notificationRule.getTenantId()));
        setName(notificationRule.getName());
        setTemplateId(getUuid(notificationRule.getTemplateId()));
        setTriggerType(notificationRule.getTriggerType());
        setTriggerConfig(toJson(notificationRule.getTriggerConfig()));
        setRecipientsConfig(toJson(notificationRule.getRecipientsConfig()));
        setAdditionalConfig(toJson(notificationRule.getAdditionalConfig()));
    }

    public NotificationRuleEntity(NotificationRuleEntity other) {
        this.id = other.id;
        this.createdTime = other.createdTime;
        this.tenantId = other.tenantId;
        this.name = other.name;
        this.templateId = other.templateId;
        this.triggerType = other.triggerType;
        this.triggerConfig = other.triggerConfig;
        this.recipientsConfig = other.recipientsConfig;
        this.additionalConfig = other.additionalConfig;
    }

    @Override
    public NotificationRule toData() {
        NotificationRule notificationRule = new NotificationRule();
        notificationRule.setId(new NotificationRuleId(id));
        notificationRule.setCreatedTime(createdTime);
        notificationRule.setTenantId(getTenantId(tenantId));
        notificationRule.setName(name);
        notificationRule.setTemplateId(getEntityId(templateId, NotificationTemplateId::new));
        notificationRule.setTriggerType(triggerType);
        notificationRule.setTriggerConfig(fromJson(triggerConfig, NotificationRuleTriggerConfig.class));
        notificationRule.setRecipientsConfig(fromJson(recipientsConfig, NotificationRuleRecipientsConfig.class));
        notificationRule.setAdditionalConfig(fromJson(additionalConfig, NotificationRuleConfig.class));
        return notificationRule;
    }

}
