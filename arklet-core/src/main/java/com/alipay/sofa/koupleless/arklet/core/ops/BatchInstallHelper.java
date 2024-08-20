/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.arklet.core.ops;

import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 合并部署帮助类。
 *
 * @author CodeNoobKingKc2
 * @version $Id: BatchInstallService, v 0.1 2023-11-20 15:35 CodeNoobKingKc2 Exp $
 */
public class BatchInstallHelper {

    /**
     * <p>getBizUrlsFromLocalFileSystem.</p>
     *
     * @param absoluteBizDirPath a {@link java.lang.String} object
     * @return a {@link java.util.List} object
     */
    @SneakyThrows
    public Map<Integer, List<String>> getBizUrlsFromLocalFileSystem(String absoluteBizDirPath) {
        Map<Integer, List<String>> bizUrlsWithPriority = new HashMap<>();
        Files.walkFileTree(new File(absoluteBizDirPath).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".jar")) {
                    Map<String, Object> attributes = getMainAttributes(absolutePath.toString());
                    if (attributes.containsKey("Ark-Biz-Name")) {
                        Integer order = Integer.valueOf(
                            attributes.getOrDefault("priority", PriorityOrdered.DEFAULT_PRECEDENCE)
                                .toString());
                        bizUrlsWithPriority.putIfAbsent(order, new ArrayList<>());
                        bizUrlsWithPriority.get(order).add(absolutePath.toString());
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });

        // reorder
        List<Map.Entry<Integer, List<String>>> keyInOrder = new ArrayList<>(
            bizUrlsWithPriority.entrySet());
        keyInOrder.sort(Map.Entry.comparingByKey());
        Map<Integer, List<String>> bizUrlsInOrder = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> entry : keyInOrder) {
            bizUrlsInOrder.put(entry.getKey(), entry.getValue());
        }

        return bizUrlsInOrder;
    }

    /**
     * 获取 biz jar 文件的主属性。
     *
     * @param bizUrl biz jar 文件路径。
     * @return 主属性。
     */
    @SneakyThrows
    public Map<String, Object> getMainAttributes(String bizUrl) {
        try (JarFile jarFile = new JarFile(bizUrl)) {
            Manifest manifest = jarFile.getManifest();
            Preconditions.checkState(manifest != null, "Manifest file not found in the JAR.");
            Map<String, Object> result = new HashMap<>();
            manifest.getMainAttributes().forEach((k, v) -> result.put(k.toString(), v));
            return result;
        }
    }
}
