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

package org.thingsboard.rule.engine.customize.bridge;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.msg.TbMsg;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * 自定义-桥,连通规则链的节点
 *
 * @author Luohh
 */
@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "bridge",
        relationTypes = {"True", "False"},
        configClazz = TbBridgeNodeConfiguration.class,
        nodeDescription = "连通规则链的节点",
        nodeDetails = "连通规则链的节点"
)
public class TbBridgeNode implements TbNode {
    private TbBridgeNodeConfiguration config;


    /**
     * 初始化,主要获取用户配置的数据
     *
     * @param ctx           前规则链节点和上下文信息的属性,常用的方法和属性
     * @param configuration 节点数据处理配置
     * @throws TbNodeException 规则链节点异常
     */
    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbBridgeNodeConfiguration.class);
    }

    /**
     * 处理业务逻辑
     */
    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException, IOException {
        boolean path = this.config.isPath();
        ctx.tellNext(msg, path ? "True" : "False");
    }

}
