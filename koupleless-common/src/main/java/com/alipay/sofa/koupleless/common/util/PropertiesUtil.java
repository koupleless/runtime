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
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.common.util.StringUtils;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * <p>PropertiesUtil class.</p>
 *
 * @author gaosaroma@gmail.com
 * @version $Id: PropertiesUtil.java, v 0.1 2024年02月26日 17:34 lipeng Exp $
 */
public class PropertiesUtil {

    /**
     * <p>loadProperties.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object
     * @param resource a {@link java.lang.String} object
     * @return a {@link java.util.Properties} object
     */
    public static Properties loadProperties(ClassLoader classLoader, String resource) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        } catch (IOException e) {
            return properties;
        }
    }

    private static Set<String> formatPropertyValues(String value) {
        return StringUtils.strToSet(value, ",");
    }

    /**
     * <p>formatPropertyValues.</p>
     *
     * @param environment a {@link org.springframework.core.env.Environment} object
     * @param key a {@link java.lang.String} object
     * @return a {@link java.util.Set} object
     */
    public static Set<String> formatPropertyValues(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (!StringUtils.isEmpty(value)) {
            return formatPropertyValues(value);
        }

        int i = 0;
        String value_with_index = environment.getProperty(key + "[" + i + "]");
        Set<String> values = new HashSet<>();
        while (!StringUtils.isEmpty(value_with_index)) {
            values.addAll(formatPropertyValues(value_with_index));
            i++;
            value_with_index = environment.getProperty(key + "[" + i + "]");
        }
        return values;
    }
}
