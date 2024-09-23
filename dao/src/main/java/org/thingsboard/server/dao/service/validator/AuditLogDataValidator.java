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
package org.thingsboard.server.dao.service.validator;

import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.service.DataValidator;

@Component
public class AuditLogDataValidator extends DataValidator<AuditLog> {

    @Override
    protected void validateDataImpl(TenantId tenantId, AuditLog auditLog) {
        if (auditLog.getEntityId() == null) {
            throw new DataValidationException("Entity Id should be specified!");
        }
        if (auditLog.getTenantId() == null) {
            throw new DataValidationException("Tenant Id should be specified!");
        }
        if (auditLog.getUserId() == null) {
            throw new DataValidationException("User Id should be specified!");
        }
    }
}
