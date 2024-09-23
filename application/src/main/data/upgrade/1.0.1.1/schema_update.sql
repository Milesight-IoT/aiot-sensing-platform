--
-- Copyright © 2016-2022 The Thingsboard Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- 创建仪表板表格部件
CREATE TABLE IF NOT EXISTS dashboard_rule_devices (
    id uuid NOT NULL,
    created_time int8,
    type varchar(255) COLLATE pg_catalog.default NOT NULL,
    value text COLLATE pg_catalog.default,
    device_id uuid NOT NULL,
    rule_name varchar(255) COLLATE pg_catalog.default,
    rule_chain_id uuid,
    tenant_id uuid NOT NULL,
    CONSTRAINT dashboard_rule_devices_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_device_id_created_time_unq_key
    ON dashboard_rule_devices USING btree (created_time pg_catalog.int8_ops DESC NULLS LAST,
                                            device_id pg_catalog.uuid_ops DESC NULLS LAST);

COMMENT ON COLUMN dashboard_rule_devices.created_time IS '创建时间';
COMMENT ON COLUMN dashboard_rule_devices.type IS '触发告警类型';
COMMENT ON COLUMN dashboard_rule_devices.value IS '触发告警值';
COMMENT ON COLUMN dashboard_rule_devices.device_id IS '设备ID';
COMMENT ON COLUMN dashboard_rule_devices.rule_name IS '规则名称';
COMMENT ON COLUMN dashboard_rule_devices.rule_chain_id IS '规则ID';
COMMENT ON COLUMN dashboard_rule_devices.tenant_id IS '租户ID';

-- 各表租户规则链信息关联表

CREATE TABLE IF NOT EXISTS rule_chain_associate (
    id uuid NOT NULL,
    created_time int8 NOT NULL,
    tenant_id uuid,
    associate_id uuid,
    rule_id uuid,
    type varchar(50) COLLATE pg_catalog.default,
    CONSTRAINT rule_node_tenant_associate_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS index_associate_id ON rule_chain_associate USING btree (
    associate_id pg_catalog.uuid_ops ASC NULLS LAST
    );

CREATE INDEX IF NOT EXISTS index_rule_node_id ON rule_chain_associate USING btree (
    rule_id pg_catalog.uuid_ops ASC NULLS LAST
    );

CREATE INDEX IF NOT EXISTS index_tenant_id ON rule_chain_associate USING btree (
    tenant_id pg_catalog.uuid_ops ASC NULLS LAST
    );


COMMENT ON COLUMN rule_chain_associate.id IS '租户规则链信息关联表主键ID';
COMMENT ON COLUMN rule_chain_associate.created_time IS '创建时间';
COMMENT ON COLUMN rule_chain_associate.tenant_id IS 'tenant.id，租户ID';
COMMENT ON COLUMN rule_chain_associate.associate_id IS '关联主键ID,根据type字段变换表';
COMMENT ON COLUMN rule_chain_associate.rule_id IS '规则链节点/规则链ID';
COMMENT ON COLUMN rule_chain_associate.type IS '关联类型';
COMMENT ON TABLE rule_chain_associate IS '各表规则链信息关联表';

-- 产品定义规则链表 （rule_chain_own）


CREATE TABLE IF NOT EXISTS rule_chain_own
(
    id            uuid NOT NULL,
    created_time  int8 NOT NULL,
    tenant_id     uuid,
    rule_chain_id uuid,
    rule_node_id  uuid,
    name          varchar(64) COLLATE pg_catalog.default,
    trigger       varchar(64) COLLATE pg_catalog.default,
    actions        varchar(255) COLLATE pg_catalog.default,
    json_data     text COLLATE pg_catalog.default,
    search_text   varchar(255) COLLATE pg_catalog.default,
    CONSTRAINT rule_chain_own_pkey PRIMARY KEY (id)
)
;

CREATE UNIQUE INDEX IF NOT EXISTS rule_chain_own_tenant_id_name_unq_key ON rule_chain_own USING btree (
    tenant_id pg_catalog.uuid_ops ASC NULLS LAST,
    name COLLATE pg_catalog.default pg_catalog.text_ops ASC NULLS LAST
    );

COMMENT ON COLUMN rule_chain_own.id IS '规则链表（二开）主键';
COMMENT ON COLUMN rule_chain_own.created_time IS '创建时间';
COMMENT ON COLUMN rule_chain_own.tenant_id IS 'tentan.id,租户ID';
COMMENT ON COLUMN rule_chain_own.rule_chain_id IS 'rule_chain.id,规则链ID';
COMMENT ON COLUMN rule_chain_own.rule_node_id IS 'rule_node.id,规则节点ID';
COMMENT ON COLUMN rule_chain_own.name IS '规则链名称';
COMMENT ON COLUMN rule_chain_own.trigger IS '触发器';
COMMENT ON COLUMN rule_chain_own.actions IS '动作（多个动作逗号隔开）';
COMMENT ON COLUMN rule_chain_own.json_data IS '不同触发器不同的对象，json';
COMMENT ON COLUMN rule_chain_own.search_text IS '查询条件';
COMMENT ON TABLE rule_chain_own IS '规则链表（二开）';


-- 接收方管理

CREATE TABLE IF NOT EXISTS recipients
(
    id                uuid NOT NULL,
    created_time      int8 NOT NULL,
    updated_time      int8,
    name              varchar(64) COLLATE pg_catalog.default,
    transmission_type varchar(30) COLLATE pg_catalog.default,
    json_data         text COLLATE pg_catalog.default,
    username          varchar(64) COLLATE pg_catalog.default,
    password          varchar(64) COLLATE pg_catalog.default,
    search_text       varchar(255) COLLATE pg_catalog.default,
    tenant_id         uuid,
    CONSTRAINT recipients_pkey PRIMARY KEY (id)
)
;


CREATE UNIQUE INDEX IF NOT EXISTS recipients_tenant_id_name_unq_key ON recipients USING btree (
    tenant_id pg_catalog.uuid_ops ASC NULLS LAST,
    name COLLATE pg_catalog.default pg_catalog.text_ops ASC NULLS LAST
    );

COMMENT ON COLUMN recipients.id IS '接收方网络配置主键ID';
COMMENT ON COLUMN recipients.created_time IS '创建时间';
COMMENT ON COLUMN recipients.updated_time IS '更新时间';
COMMENT ON COLUMN recipients.name IS '名称';
COMMENT ON COLUMN recipients.transmission_type IS '网络传输类型，1：http，2：mqtt';
COMMENT ON COLUMN recipients.json_data IS '不同传输类型各种配置';
COMMENT ON COLUMN recipients.username IS '用户名';
COMMENT ON COLUMN recipients.password IS '密码';
COMMENT ON COLUMN recipients.search_text IS '查询字段';
COMMENT ON COLUMN recipients.tenant_id IS 'tenant.id,租户ID';
COMMENT ON TABLE recipients IS '接收方网络配置管理表';

-- DELETE_DATA
DELETE FROM telemetry_recognition
WHERE id IN (
    SELECT id FROM ( SELECT ROW_NUMBER ( ) OVER ( PARTITION BY device_id, ability, ts ORDER BY ts DESC ) AS rn, ID FROM telemetry_recognition ) T
    WHERE T.rn <> 1
);

-- MODIFY_INDEX

DROP INDEX IF EXISTS idx_device_id_ability_ts ;
CREATE UNIQUE INDEX IF NOT EXISTS idx_device_id_ability_ts ON PUBLIC.telemetry_recognition USING btree (
    device_id pg_catalog.uuid_ops ASC NULLS LAST,
    ability COLLATE pg_catalog.DEFAULT pg_catalog.text_ops ASC NULLS LAST,
    ts pg_catalog.int8_ops ASC NULLS LAST
    );

UPDATE queue SET pack_processing_timeout = 4000 WHERE topic = 'tb_rule_engine.main'