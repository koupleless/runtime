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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides isolation for multi-business environment configurations by separating
 * the `System.getenv()` variables for different business modules. This ensures
 * that each module has its own isolated configuration, while default values from
 * the base application are used where applicable.
 *
 * <p>
 * To enable this feature, initialize it in your base application with the following code:
 * </p>
 * <code>
 * IsolatedEnvironment.takeover();
 * </code>
 *
 * @author oxsean@gmail.com
 * @version 1.0.0
 */
public class IsolatedEnvironment extends AbstractMap<String, String> {

    private static final String            BIZ_CLASS_LOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";
    private static final String            EMPTY            = "";
    private static Method                  GET_BIZ_MODEL_METHOD;
    private static Method                  GET_BIZ_NAME;

    private final String                   bizClassLoaderName;
    private final Map<String, String>      original;
    private final Map<String, String>      additional;
    private final Set<String>              keys;
    private final Map<ClassLoader, String> cache            = new ConcurrentHashMap<>();

    private IsolatedEnvironment(String bizClassLoaderName, Map<String, String> original,
                                Map<String, String> additional, Set<String> keys) {
        this.bizClassLoaderName = bizClassLoaderName;
        this.original = original;
        this.additional = additional;
        this.keys = keys;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean containsKey(Object key) {
        String prefix = getKeyPrefix();
        return !EMPTY.equals(prefix) && keys.contains(prefix + key) || keys.contains(key);
    }

    @Override
    public String get(Object key) {
        String prefix = getKeyPrefix();
        if (!additional.isEmpty()) {
            String value = get(additional, prefix, key);
            if (value != null) {
                return value;
            }
        }
        return get(original, prefix, key);
    }

    private static String get(Map<String, String> map, String prefix, Object key) {
        if (!EMPTY.equals(prefix)) {
            String value = map.get(prefix + key);
            if (value != null) {
                return value;
            }
        }
        return map.get(key);
    }

    private String getKeyPrefix() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl == null ? EMPTY : cache.computeIfAbsent(tccl, this::readKeyPrefix);
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                Iterator<String> it = keys.iterator();
                return new Iterator<Entry<String, String>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<String, String> next() {
                        String key = it.next();
                        return new Entry<String, String>() {
                            @Override
                            public String getKey() {
                                return key;
                            }

                            @Override
                            public String getValue() {
                                return get(key);
                            }

                            @Override
                            public String setValue(String value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return keys.size();
            }
        };
    }

    public static Map<String, String> takeover(String bizClassLoaderName,
                                               Map<String, String> additionalEnv) {
        Objects.requireNonNull(bizClassLoaderName, "bizClassLoaderName");
        Objects.requireNonNull(additionalEnv, "additionalEnv");

        try {
            Class<?> envClass = Class.forName("java.lang.ProcessEnvironment");

            Field unmodifiableField = envClass.getDeclaredField("theUnmodifiableEnvironment");
            Map<String, String> unmodifiableEnv = UnsafeUtils.readStaticField(envClass,
                unmodifiableField);
            if (unmodifiableEnv instanceof IsolatedEnvironment) {
                return ((IsolatedEnvironment) unmodifiableEnv).original;
            }

            Set<String> unmodifiableKeys;
            if (additionalEnv.isEmpty()) {
                unmodifiableKeys = unmodifiableEnv.keySet();
            } else {
                unmodifiableKeys = new HashSet<>(unmodifiableEnv.size() + additionalEnv.size());
                unmodifiableKeys.addAll(unmodifiableEnv.keySet());
                unmodifiableKeys.addAll(additionalEnv.keySet());
            }
            IsolatedEnvironment unmodifiableEnvWrapper = new IsolatedEnvironment(bizClassLoaderName,
                unmodifiableEnv, additionalEnv, unmodifiableKeys);
            UnsafeUtils.writeStaticField(envClass, unmodifiableField, unmodifiableEnvWrapper);

            try {
                Field caseInsensitiveField = envClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
                Map<String, String> caseInsensitiveEnv = UnsafeUtils.readStaticField(envClass,
                    caseInsensitiveField);

                Set<String> caseInsensitiveKeys;
                Map<String, String> caseInsensitiveAdditionalEnv;
                if (additionalEnv.isEmpty()) {
                    caseInsensitiveKeys = caseInsensitiveEnv.keySet();
                    caseInsensitiveAdditionalEnv = additionalEnv;
                } else {
                    caseInsensitiveKeys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    caseInsensitiveKeys.addAll(caseInsensitiveEnv.keySet());
                    caseInsensitiveKeys.addAll(additionalEnv.keySet());
                    caseInsensitiveAdditionalEnv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    caseInsensitiveAdditionalEnv.putAll(additionalEnv);
                }
                IsolatedEnvironment caseInsensitiveEnvWrapper = new IsolatedEnvironment(
                    bizClassLoaderName, caseInsensitiveEnv, caseInsensitiveAdditionalEnv,
                    caseInsensitiveKeys);
                UnsafeUtils.writeStaticField(envClass, caseInsensitiveField,
                    caseInsensitiveEnvWrapper);
            } catch (NoSuchFieldException ignored) {
                // theCaseInsensitiveEnvironment not exists in linux platform
            }

            return unmodifiableEnv;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize isolated environment", e);
        }
    }

    public static Map<String, String> takeover(Map<String, String> additionalEnv) {
        return takeover(BIZ_CLASS_LOADER, additionalEnv);
    }

    public static Map<String, String> takeover(String bizClassLoaderName) {
        return takeover(bizClassLoaderName, Collections.emptyMap());
    }

    public static Map<String, String> takeover() {
        return takeover(BIZ_CLASS_LOADER, Collections.emptyMap());
    }

    protected String readKeyPrefix(ClassLoader classLoader) {
        while (classLoader != null) {
            if (isBizClassLoader(classLoader.getClass())) {
                break;
            }
            classLoader = classLoader.getParent();
        }
        if (classLoader != null) {
            try {
                if (GET_BIZ_MODEL_METHOD == null) {
                    GET_BIZ_MODEL_METHOD = classLoader.getClass().getMethod("getBizModel");
                }
                Object bizModel = GET_BIZ_MODEL_METHOD.invoke(classLoader);
                if (bizModel != null) {
                    if (GET_BIZ_NAME == null) {
                        GET_BIZ_NAME = bizModel.getClass().getMethod("getBizName");
                    }
                    String value = (String) GET_BIZ_NAME.invoke(bizModel);
                    if (value != null) {
                        return value.replaceAll("[-.]", "_") + '_';
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return EMPTY;
    }

    private boolean isBizClassLoader(Class<?> clazz) {
        while (clazz != null && ClassLoader.class.isAssignableFrom(clazz)) {
            if (bizClassLoaderName.equals(clazz.getName())) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}
