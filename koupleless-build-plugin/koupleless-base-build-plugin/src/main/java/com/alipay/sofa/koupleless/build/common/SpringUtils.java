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
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
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
        Properties properties = new Properties();
        properties.load(inputStream);
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                String[] values = value.split("\\s*,\\s*");
                result.put(key, Arrays.asList(values));
            }
        }
        return result;
    }
}
