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
package org.thingsboard.server.common.data.notification.template;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.server.common.data.notification.NotificationDeliveryMethod;
import org.thingsboard.server.common.data.validation.Length;
import org.thingsboard.server.common.data.validation.NoXss;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmailDeliveryMethodNotificationTemplate extends DeliveryMethodNotificationTemplate implements HasSubject {

    @NoXss(fieldName = "email subject")
    @Length(fieldName = "email subject", max = 250, message = "cannot be longer than 250 chars")
    @NotEmpty
    private String subject;

    public EmailDeliveryMethodNotificationTemplate(EmailDeliveryMethodNotificationTemplate other) {
        super(other);
        this.subject = other.subject;
    }

    @Override
    public NotificationDeliveryMethod getMethod() {
        return NotificationDeliveryMethod.EMAIL;
    }

    @Override
    public EmailDeliveryMethodNotificationTemplate copy() {
        return new EmailDeliveryMethodNotificationTemplate(this);
    }

    @Override
    public boolean containsAny(String... params) {
        return super.containsAny(params) || StringUtils.containsAny(subject, params);
    }

}
