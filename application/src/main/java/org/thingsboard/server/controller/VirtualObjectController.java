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
package org.thingsboard.server.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.queue.util.TbCoreComponent;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class VirtualObjectController extends BaseController {
//    @Autowired
//    private VirtualObjectService virtualObjectService;

    @ApiOperation("获取虚拟对象列表")
    @RequestMapping(value = "/virtual/objects", method = RequestMethod.GET)
    public void deviceAbilities() {
    }

    @ApiOperation("新增虚拟对象")
    @RequestMapping(value = "/virtual/object", method = RequestMethod.POST)
    public void saveDeviceAbility() {
    }

    @ApiOperation("删除虚拟对象")
    @RequestMapping(value = "/virtual/object/{id}", method = RequestMethod.DELETE)
    public void deleteDeviceAbility() {
    }
}
