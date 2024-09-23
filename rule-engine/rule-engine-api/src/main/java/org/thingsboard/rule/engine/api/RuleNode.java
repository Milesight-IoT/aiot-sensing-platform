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
package org.thingsboard.rule.engine.api;

import org.thingsboard.server.common.data.plugin.ComponentClusteringMode;
import org.thingsboard.server.common.data.plugin.ComponentScope;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.rule.RuleChainType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 规则节点配置注解
 *
 * @author Luohh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RuleNode {

    /**
     * type是可用的Rule Node Types之一。此参数影响规则链编辑器的哪个部分将包含您的规则节点；
     *
     * @return {@link ComponentType}
     */
    ComponentType type();

    /**
     * 将用于规则链编辑器和调试消息的规则节点的任何合理名称
     */
    String name();

    /**
     * 节点的简短描述。在规则链编辑器中可见
     */
    String nodeDescription();

    /**
     * 节点的简短描述。在规则链编辑器中可见
     */
    String nodeDetails();

    /**
     * 描述配置 json 的类的完整类名
     */
    Class<? extends NodeConfiguration<?>> configClazz();

    RuleChainType[] ruleChainTypes() default {RuleChainType.CORE, RuleChainType.EDGE};

    ComponentClusteringMode clusteringMode() default ComponentClusteringMode.ENABLED;

    boolean inEnabled() default true;

    boolean outEnabled() default true;

    ComponentScope scope() default ComponentScope.TENANT;

    String[] relationTypes() default {"Success", "Failure"};

    String[] uiResources() default {};

    String configDirective() default "";

    String icon() default "";

    String iconUrl() default "";

    String docUrl() default "";

    boolean customRelations() default false;

    boolean ruleChainNode() default false;


}
