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

package org.thingsboard.server.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author luo.hh
 */
@Slf4j
public class ExecuteCommandUtil {

    public static void deleteExpiredCassandraDbFiles(long ts) {
        try {
            String baseDirectory = "/data/cassandra/data/msaiotsensingplatform/";
            String time = dateConversion(ts);
            // 构建动态路径// 使用 * 通配符匹配任意字符
            String dynamicDirectoryName = "ts_kv_cf-*";
            String dynamicPath = baseDirectory + dynamicDirectoryName;
            // 构建命令
            String command = "find " + dynamicPath + " -type f ! -newermt " + time + " -exec rm {} \\;";
            log.info("Execute delete expired cassandra db files ,command = {}", command);
            // 创建进程构建器
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            // 启动进程
            Process process = processBuilder.start();
            // 等待进程执行完毕
            int exitCode = process.waitFor();
            // 打印命令的执行结果
            log.info("Command executed with exit code: {}", exitCode);
        } catch (IOException | InterruptedException e) {
            log.error("Execute delete expired cassandra db files failed", e);
        }
    }

    private static String dateConversion(long ts) {
        // 转换为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // 格式化为字符串
        return dateTime.format(formatter);
    }

    public static void nodetool(String order) {
        try {
            log.info("Execute nodetool ,command = {}", order);
            // 创建进程构建器
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", order);
            // 启动进程
            Process process = processBuilder.start();
            // 等待进程执行完毕
            int exitCode = process.waitFor();
            // 打印命令的执行结果
            log.info("Command executed with exit code: {}", exitCode);
        } catch (IOException | InterruptedException e) {
            log.error("Execute nodetool failed", e);
        }
    }
}
