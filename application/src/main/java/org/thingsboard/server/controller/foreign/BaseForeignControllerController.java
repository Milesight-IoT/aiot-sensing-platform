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

package org.thingsboard.server.controller.foreign;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thingsboard.server.cache.ms.LoginCacheMs;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.UserPrincipal;

import javax.servlet.http.HttpServletRequest;

/**
 * 对外基本 - 接口
 *
 * @author Luohh
 * @version 1.0
 * @date 2023/4/23 14:22
 */
@RestController
@TbCoreComponent
public class BaseForeignControllerController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private LoginCacheMs loginCache;

    /***
     * 验证登陆信息
     */
    public void verifyLogin() throws ThingsboardException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        String username;
        String password;
        try {
            HttpServletRequest request = requestAttributes.getRequest();
            // 解析代码
            String authorization = request.getHeader("Authorization");
            // 固定格式 Basic ********;
            String basic = authorization.replace("Basic ", "");
            String authorizationKey = new String(Base64.decodeBase64(basic));
            String[] split = authorizationKey.split(StringUtils.SEMICOLON);
            username = split[0];
            password = split[1];
        } catch (Exception e) {
            throw new ThingsboardException("account password verification failed! ", ThingsboardErrorCode.AUTHENTICATION);
        }

        String cachePassword = loginCache.get(username);
        if (StringUtils.isNotBlank(cachePassword) && cachePassword.equals(password)) {
            // 刚刚验证过了 2小时不再校验
            return;
        }
        // 将用户输入封装
        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal, password);
        // 调用authenticationManager.authenticate()方法进行验证
        authenticationManager.authenticate(authenticationToken);
        // 设置缓存
        loginCache.put(username, password);
    }
}