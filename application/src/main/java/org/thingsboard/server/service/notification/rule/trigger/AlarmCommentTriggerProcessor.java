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
package org.thingsboard.server.service.notification.rule.trigger;

import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmComment;
import org.thingsboard.server.common.data.alarm.AlarmCommentType;
import org.thingsboard.server.common.data.alarm.AlarmInfo;
import org.thingsboard.server.common.data.alarm.AlarmStatusFilter;
import org.thingsboard.server.common.data.notification.info.AlarmCommentNotificationInfo;
import org.thingsboard.server.common.data.notification.info.RuleOriginatedNotificationInfo;
import org.thingsboard.server.common.data.notification.rule.trigger.AlarmCommentNotificationRuleTriggerConfig;
import org.thingsboard.server.common.data.notification.rule.trigger.NotificationRuleTriggerType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.notification.trigger.RuleEngineMsgTrigger;

import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.thingsboard.server.common.data.util.CollectionsUtil.emptyOrContains;

@Service
public class AlarmCommentTriggerProcessor implements RuleEngineMsgNotificationRuleTriggerProcessor<AlarmCommentNotificationRuleTriggerConfig> {

    @Override
    public boolean matchesFilter(RuleEngineMsgTrigger trigger, AlarmCommentNotificationRuleTriggerConfig triggerConfig) {
        TbMsg msg = trigger.getMsg();
        if (msg.getMetaData().getValue("comment") == null) {
            return false;
        }
        if (msg.getType().equals(DataConstants.COMMENT_UPDATED) && !triggerConfig.isNotifyOnCommentUpdate()) {
            return false;
        }
        if (triggerConfig.isOnlyUserComments()) {
            AlarmComment comment = JacksonUtil.fromString(msg.getMetaData().getValue("comment"), AlarmComment.class);
            if (comment.getType() == AlarmCommentType.SYSTEM) {
                return false;
            }
        }
        Alarm alarm = JacksonUtil.fromString(msg.getData(), Alarm.class);
        return emptyOrContains(triggerConfig.getAlarmTypes(), alarm.getType()) &&
                emptyOrContains(triggerConfig.getAlarmSeverities(), alarm.getSeverity()) &&
                (isEmpty(triggerConfig.getAlarmStatuses()) || AlarmStatusFilter.from(triggerConfig.getAlarmStatuses()).matches(alarm));
    }

    @Override
    public RuleOriginatedNotificationInfo constructNotificationInfo(RuleEngineMsgTrigger trigger) {
        TbMsg msg = trigger.getMsg();
        AlarmComment comment = JacksonUtil.fromString(msg.getMetaData().getValue("comment"), AlarmComment.class);
        AlarmInfo alarmInfo = JacksonUtil.fromString(msg.getData(), AlarmInfo.class);
        return AlarmCommentNotificationInfo.builder()
                .comment(comment.getComment().get("text").asText())
                .action(msg.getType().equals(DataConstants.COMMENT_CREATED) ? "added" : "updated")
                .userEmail(msg.getMetaData().getValue("userEmail"))
                .userFirstName(msg.getMetaData().getValue("userFirstName"))
                .userLastName(msg.getMetaData().getValue("userLastName"))
                .alarmId(alarmInfo.getUuidId())
                .alarmType(alarmInfo.getType())
                .alarmOriginator(alarmInfo.getOriginator())
                .alarmOriginatorName(alarmInfo.getOriginatorName())
                .alarmSeverity(alarmInfo.getSeverity())
                .alarmStatus(alarmInfo.getStatus())
                .alarmCustomerId(alarmInfo.getCustomerId())
                .build();
    }

    @Override
    public NotificationRuleTriggerType getTriggerType() {
        return NotificationRuleTriggerType.ALARM_COMMENT;
    }

    @Override
    public Set<String> getSupportedMsgTypes() {
        return Set.of(DataConstants.COMMENT_CREATED, DataConstants.COMMENT_UPDATED);
    }

}
