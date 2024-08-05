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
package com.alipay.sofa.koupleless.common.service;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.sofa.koupleless.common.util.ClassUtils.getSuperClasses;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MainBizApplicationContext.java, v 0.1 2024年08月03日 21:22 立蓬 Exp $
 */
public class MainBizApplicationContext {
    // 以key为索引，默认为实现类的全限定名，这样不会冲突
    private Map<String, Object>                objectMap = new ConcurrentHashMap<>();

    // 以Class为索引(可以是接口Class或者实现Class)
    private Map<Class<?>, Map<String, Object>> typeMap   = new ConcurrentHashMap<>();

    public static void init() {
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContextByClassLoader(Thread.currentThread().getContextClassLoader());
        bizRuntimeContext.setMainBizApplicationContext(new MainBizApplicationContext());
    }

    public Object getObject(String key) {
        return objectMap.get(key);
    }

    public <T> Map<String, T> getObjectMap(Class<T> type) {
        return (Map<String, T>) ImmutableMap
            .copyOf(typeMap.getOrDefault(type, new ConcurrentHashMap<>()));
    }

    public void register(Object obj) {
        String key = obj.getClass().getName();
        register(key, obj);
        register(obj.getClass(), key, obj);

        Class<?>[] interfaces = obj.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(i -> register(i, key, obj));

        getSuperClasses(obj.getClass()).forEach(i -> register(i, key, obj));
    }

    /**
     * <p>register.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param obj a Object object
     */
    private void register(String key, Object obj) {
        objectMap.put(key, obj);
    }

    /**
     * <p>register.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param obj a Object object
     */
    private void register(Class<?> type, String key, Object obj) {
        Map<String, Object> map = typeMap.getOrDefault(type, new ConcurrentHashMap<>());
        map.put(key, obj);
        typeMap.put(type, map);
    }
}