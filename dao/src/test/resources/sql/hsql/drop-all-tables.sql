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

/*
 * Copyright © 2016-2023 The Thingsboard Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * *
 *  * Copyright © 2016-2023 The Thingsboard Authors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

DROP TABLE IF EXISTS admin_settings;
DROP TABLE IF EXISTS entity_alarm;
DROP TABLE IF EXISTS alarm;
DROP TABLE IF EXISTS asset;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS attribute_kv;
DROP TABLE IF EXISTS component_descriptor;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS device;
DROP TABLE IF EXISTS device_credentials;
DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS relation;
DROP TABLE IF EXISTS tb_user;
DROP TABLE IF EXISTS tenant;
DROP TABLE IF EXISTS ts_kv;
DROP TABLE IF EXISTS ts_kv_dictionary;
DROP TABLE IF EXISTS ts_kv_latest;
DROP TABLE IF EXISTS user_credentials;
DROP TABLE IF EXISTS widget_type;
DROP TABLE IF EXISTS widgets_bundle;
DROP TABLE IF EXISTS entity_view;
DROP TABLE IF EXISTS device_profile;
DROP TABLE IF EXISTS tenant_profile;
DROP TABLE IF EXISTS dashboard;
DROP TABLE IF EXISTS rule_node_state;
DROP TABLE IF EXISTS rule_node;
DROP TABLE IF EXISTS rule_chain;
DROP TABLE IF EXISTS oauth2_mobile;
DROP TABLE IF EXISTS oauth2_domain;
DROP TABLE IF EXISTS oauth2_registration;
DROP TABLE IF EXISTS oauth2_params;
DROP TABLE IF EXISTS oauth2_client_registration_template;
DROP TABLE IF EXISTS oauth2_client_registration;
DROP TABLE IF EXISTS oauth2_client_registration_info;
DROP TABLE IF EXISTS api_usage_state;
DROP TABLE IF EXISTS resource;
DROP TABLE IF EXISTS ota_package;
DROP TABLE IF EXISTS edge;
DROP TABLE IF EXISTS edge_event;
DROP TABLE IF EXISTS rpc;
DROP TABLE IF EXISTS queue;
DROP FUNCTION IF EXISTS to_uuid;
