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
package org.thingsboard.server.common.data.tenant.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.ApiUsageRecordKey;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.TenantProfileType;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DefaultTenantProfileConfiguration implements TenantProfileConfiguration {

    private long maxDevices;
    private long maxAssets;
    private long maxCustomers;
    private long maxUsers;
    private long maxDashboards;
    private long maxRuleChains;
    private long maxResourcesInBytes;
    private long maxOtaPackagesInBytes;

    private String transportTenantMsgRateLimit;
    private String transportTenantTelemetryMsgRateLimit;
    private String transportTenantTelemetryDataPointsRateLimit;
    private String transportDeviceMsgRateLimit;
    private String transportDeviceTelemetryMsgRateLimit;
    private String transportDeviceTelemetryDataPointsRateLimit;

    private String tenantEntityExportRateLimit;
    private String tenantEntityImportRateLimit;
    private String tenantNotificationRequestsRateLimit;
    private String tenantNotificationRequestsPerRuleRateLimit;

    private long maxTransportMessages;
    private long maxTransportDataPoints;
    private long maxREExecutions;
    private long maxJSExecutions;
    private long maxDPStorageDays;
    private int maxRuleNodeExecutionsPerMessage;
    private long maxEmails;
    private long maxSms;
    private long maxCreatedAlarms;

    private String tenantServerRestLimitsConfiguration;
    private String customerServerRestLimitsConfiguration;

    private int maxWsSessionsPerTenant;
    private int maxWsSessionsPerCustomer;
    private int maxWsSessionsPerRegularUser;
    private int maxWsSessionsPerPublicUser;
    private int wsMsgQueueLimitPerSession;
    private long maxWsSubscriptionsPerTenant;
    private long maxWsSubscriptionsPerCustomer;
    private long maxWsSubscriptionsPerRegularUser;
    private long maxWsSubscriptionsPerPublicUser;
    private String wsUpdatesPerSessionRateLimit;

    private String cassandraQueryTenantRateLimitsConfiguration;

    private int defaultStorageTtlDays;
    private int alarmsTtlDays;
    private int rpcTtlDays;

    private double warnThreshold;

    @Override
    public long getProfileThreshold(ApiUsageRecordKey key) {
        switch (key) {
            case TRANSPORT_MSG_COUNT:
                return maxTransportMessages;
            case TRANSPORT_DP_COUNT:
                return maxTransportDataPoints;
            case JS_EXEC_COUNT:
                return maxJSExecutions;
            case RE_EXEC_COUNT:
                return maxREExecutions;
            case STORAGE_DP_COUNT:
                return maxDPStorageDays;
            case EMAIL_EXEC_COUNT:
                return maxEmails;
            case SMS_EXEC_COUNT:
                return maxSms;
            case CREATED_ALARMS_COUNT:
                return maxCreatedAlarms;
        }
        return 0L;
    }

    @Override
    public long getWarnThreshold(ApiUsageRecordKey key) {
        return (long) (getProfileThreshold(key) * (warnThreshold > 0.0 ? warnThreshold : 0.8));
    }

    public long getEntitiesLimit(EntityType entityType) {
        switch (entityType) {
            case DEVICE:
                return maxDevices;
            case ASSET:
                return maxAssets;
            case CUSTOMER:
                return maxCustomers;
            case USER:
                return maxUsers;
            case DASHBOARD:
                return maxDashboards;
            case RULE_CHAIN:
                return maxRuleChains;
            default:
                return 0;
        }
    }

    @Override
    public TenantProfileType getType() {
        return TenantProfileType.DEFAULT;
    }

    @Override
    public int getMaxRuleNodeExecsPerMessage() {
        return maxRuleNodeExecutionsPerMessage;
    }
}
