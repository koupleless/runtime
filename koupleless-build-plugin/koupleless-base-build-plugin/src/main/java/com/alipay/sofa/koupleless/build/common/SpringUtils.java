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
package com.alipay.sofa.koupleless.build.common;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class SpringUtils {

    private static SpringUtils instance = new SpringUtils();

    public static SpringUtils INSTANCE() {
        return instance;
    }

    @SneakyThrows
    public Map<String, List<String>> parseSpringFactoryConfig(InputStream inputStream) {
        Map<String, List<String>> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder currentLine = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().endsWith("\\")) {
                    currentLine.append(line.substring(0, line.length() - 1)); // Remove '\'
                } else {
                    currentLine.append(line);
                    String[] keyValue = currentLine.toString().split("=", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String[] values = keyValue[1].trim().split("\\s*,\\s*"); // Split on comma with optional spaces
                        result.put(key, (List<String>) Arrays.asList(values));
                    }
                    currentLine = new StringBuilder();
                }
            }
        }
        return result;
    }
}
