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
package com.alipay.sofa.koupleless.common.model;

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
public class MainApplicationContext {
    // default key: class Name, also can be set by alias
    private Map<String, Object>                objectMap = new ConcurrentHashMap<>();

    // 以Class为索引(可以是接口Class或者实现Class)
    private Map<Class<?>, Map<String, Object>> typeMap   = new ConcurrentHashMap<>();

    public Object getObject(String key) {
        return objectMap.get(key);
    }

    public <T> Map<String, T> getObjectMap(Class<T> type) {
        return (Map<String, T>) ImmutableMap
            .copyOf(typeMap.getOrDefault(type, new ConcurrentHashMap<>()));
    }

    public void register(Object obj) {
        doRegister(obj.getClass().getName(), obj);
    }

    public void register(String alias, Object obj) {
        doRegister(obj.getClass().getName(), obj);
        doRegister(alias, obj);
    }

    public void close() {
        objectMap.clear();
        typeMap.clear();
    }

    private void doRegister(String key, Object obj) {
        // register by key
        innerRegister(key, obj);

        // register by class type
        innerRegister(obj.getClass(), key, obj);

        // register by interface type
        Class<?>[] interfaces = obj.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(i -> innerRegister(i, key, obj));

        // register by super class
        getSuperClasses(obj.getClass()).forEach(i -> innerRegister(i, key, obj));
    }

    /**
     * <p>innerRegister.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param obj a Object object
     */
    private void innerRegister(String key, Object obj) {
        objectMap.put(key, obj);
    }

    /**
     * <p>innerRegister.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param obj a Object object
     */
    private void innerRegister(Class<?> type, String key, Object obj) {
        Map<String, Object> map = typeMap.getOrDefault(type, new ConcurrentHashMap<>());
        map.put(key, obj);
        typeMap.put(type, map);
    }
}