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
package com.alipay.sofa.koupleless.utils;

import java.lang.reflect.Field;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ReflectionUtils.java, v 0.1 2024年07月22日 10:37 立蓬 Exp $
 */
public class ReflectionUtils {
    /**
     * Set field of specified object to value, will try to operate on super class until success
     *
     * @param fieldName a {@link java.lang.String} object
     * @param o a {@link java.lang.Object} object
     * @param value a T object
     * @param <T> a T class
     */
    public static <T> void setField(String fieldName, Object o, T value) {
        Class<?> klass = o.getClass();
        while (klass != null) {
            try {
                Field f = klass.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(o, value);
                return;
            } catch (Exception e) {
                klass = klass.getSuperclass();
            }
        }
    }
}