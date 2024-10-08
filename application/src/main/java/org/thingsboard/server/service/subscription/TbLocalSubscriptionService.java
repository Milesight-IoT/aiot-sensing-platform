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
package org.thingsboard.server.service.subscription;

import org.thingsboard.server.common.msg.queue.TbCallback;
import org.thingsboard.server.queue.discovery.event.ClusterTopologyChangeEvent;
import org.thingsboard.server.queue.discovery.event.PartitionChangeEvent;
import org.thingsboard.server.service.ws.notification.sub.NotificationsSubscriptionUpdate;
import org.thingsboard.server.service.ws.telemetry.sub.AlarmSubscriptionUpdate;
import org.thingsboard.server.service.ws.telemetry.sub.TelemetrySubscriptionUpdate;

public interface TbLocalSubscriptionService {

    void addSubscription(TbSubscription subscription);

    void cancelSubscription(String sessionId, int subscriptionId);

    void cancelAllSessionSubscriptions(String sessionId);

    void onSubscriptionUpdate(String sessionId, TelemetrySubscriptionUpdate update, TbCallback callback);

    void onSubscriptionUpdate(String sessionId, AlarmSubscriptionUpdate update, TbCallback callback);

    void onSubscriptionUpdate(String sessionId, int subscriptionId, NotificationsSubscriptionUpdate update, TbCallback callback);

    void onApplicationEvent(PartitionChangeEvent event);

    void onApplicationEvent(ClusterTopologyChangeEvent event);
}
