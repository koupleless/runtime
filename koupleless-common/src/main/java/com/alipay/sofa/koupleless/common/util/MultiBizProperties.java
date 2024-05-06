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

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Support multi-business Properties
 * Isolating configuration separation between different business modules
 * The default value of the configuration of the base application is used
 * <p>
 * If you want to use, you need to write the code in you base application
 * </p>
 * <code>
 * MultiBizProperties.initSystem();
 * </code>
 *
 * @author qq290584697
 * @version 1.0.0
 */
public class MultiBizProperties extends Properties {

    private final String                  bizClassLoaderName;

    private static final String           BIZ_CLASS_LOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    private Map<ClassLoader, Set<String>> modifiedKeysMap  = new HashMap<>();

    private final Properties              baseProperties;
    private Map<ClassLoader, Properties>  bizPropertiesMap;

    private MultiBizProperties(String bizClassLoaderName, Properties baseProperties) {
        this.bizPropertiesMap = new HashMap<>();
        this.baseProperties = baseProperties;
        this.bizClassLoaderName = bizClassLoaderName;
    }

    /**
     * <p>Constructor for MultiBizProperties.</p>
     *
     * @param bizClassLoaderName a {@link java.lang.String} object
     */
    public MultiBizProperties(String bizClassLoaderName) {
        this(bizClassLoaderName, new Properties());
    }

    /** {@inheritDoc} */
    public synchronized Object setProperty(String key, String value) {
        addModifiedKey(key);
        return getWriteProperties().setProperty(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public String getProperty(String key) {
        return getReadProperties().getProperty(key);
    }

    /** {@inheritDoc} */
    @Override
    public String getProperty(String key, String defaultValue) {
        return getReadProperties().getProperty(key, defaultValue);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void load(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inStream);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    /** {@inheritDoc} */
    @Override
    public void list(PrintStream out) {
        getWriteProperties().list(out);
    }

    /** {@inheritDoc} */
    @Override
    public void list(PrintWriter out) {
        getWriteProperties().list(out);
    }

    /** {@inheritDoc} */
    @Override
    public void save(OutputStream out, String comments) {
        Properties properties = getWriteProperties();
        properties.save(out, comments);
    }

    /** {@inheritDoc} */
    @Override
    public void store(Writer writer, String comments) throws IOException {
        Properties properties = getReadProperties();
        properties.store(writer, comments);
    }

    /** {@inheritDoc} */
    @Override
    public void store(OutputStream out, String comments) throws IOException {
        Properties properties = getReadProperties();
        properties.store(out, comments);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(in);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    /** {@inheritDoc} */
    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        Properties properties = getReadProperties();
        properties.storeToXML(os, comment);
    }

    /** {@inheritDoc} */
    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        Properties properties = getReadProperties();
        properties.storeToXML(os, comment, encoding);
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<?> propertyNames() {
        return getReadProperties().propertyNames();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> stringPropertyNames() {
        return getReadProperties().stringPropertyNames();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean remove(Object key, Object value) {
        boolean success = getWriteProperties().remove(key, value);
        if (success) {
            addModifiedKey(key.toString());
        }
        return success;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object get(Object key) {
        return getReadProperties().get(key);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object remove(Object key) {
        if (key != null) {
            addModifiedKey(key.toString());
        }
        return getWriteProperties().remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object put(Object key, Object value) {
        String text = key == null ? null : key.toString();
        addModifiedKey(text);
        return getWriteProperties().put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean equals(Object o) {
        return getReadProperties().equals(o);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String toString() {
        return getReadProperties().toString();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Object> values() {
        return getReadProperties().values();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized int hashCode() {
        return getReadProperties().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void clear() {
        Set<String> keys = baseProperties.stringPropertyNames();
        getWriteProperties().clear();
        addModifiedKeys(keys);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object clone() {
        MultiBizProperties mbp = new MultiBizProperties(bizClassLoaderName, baseProperties);
        mbp.bizPropertiesMap = new HashMap<>();
        bizPropertiesMap.forEach((k, p) -> mbp.bizPropertiesMap.put(k, (Properties) p.clone()));
        mbp.bizPropertiesMap.putAll(bizPropertiesMap);
        mbp.modifiedKeysMap = new HashMap<>();
        modifiedKeysMap.forEach((k, s) -> mbp.modifiedKeysMap.put(k, new HashSet<>(s)));
        return mbp;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isEmpty() {
        return getReadProperties().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object replace(Object key, Object value) {
        Object curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean containsKey(Object key) {
        return getReadProperties().containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean contains(Object value) {
        return getReadProperties().contains(value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        Map map = new HashMap();
        for (Map.Entry entry : entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            v = function.apply(k, v);
            map.put(k, v);
        }
        putAll(map);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized int size() {
        return getReadProperties().size();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getReadProperties().entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void putAll(Map map) {
        Set<String> keys = new HashSet<>();
        for (Object key : map.keySet()) {
            String text = key == null ? null : key.toString();
            keys.add(text);
        }
        addModifiedKeys(keys);
        getWriteProperties().putAll(map);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object computeIfAbsent(Object key,
                                               Function<? super Object, ?> mappingFunction) {
        Object value = get(key);
        if (value == null) {
            Object newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Enumeration<Object> elements() {
        return getReadProperties().elements();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        getReadProperties().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        Object v = get(key);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Enumeration<Object> keys() {
        return getReadProperties().keys();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Object> keySet() {
        return getReadProperties().keySet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return getReadProperties().containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return getReadProperties().getOrDefault(key, defaultValue);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object computeIfPresent(Object key,
                                                BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        if (oldValue == null) {
            return null;
        }
        Object newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            put(key, newValue);
            return newValue;
        }
        remove(key);
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object compute(Object key,
                                       BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        Object newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            if (oldValue != null || containsKey(key)) {
                remove(key);
            }
            return null;
        }
        put(key, newValue);
        return newValue;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Object merge(Object key, Object value,
                                     BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        Object newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    /**
     * 获得当前业务模块的类加载器
     * 如果当前线程不属于业务模块，而是基座，将返回null
     *
     * @return 当前业务模块的类加载器
     */
    public ClassLoader getBizClassLoader() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        return getBizClassLoader(invokeClassLoader);
    }

    private synchronized Properties getReadProperties() {
        Properties bizProperties = getWriteProperties();
        if (bizProperties == baseProperties) {
            return baseProperties;
        }
        Properties properties = new Properties();
        properties.putAll(baseProperties);
        Set<String> modifiedKeys = getModifiedKeys();
        if (modifiedKeys != null) {
            modifiedKeys.forEach(properties::remove);
        }
        properties.putAll(bizProperties);
        return properties;
    }

    private ClassLoader getBizClassLoader(ClassLoader invokeClassLoader) {
        for (ClassLoader classLoader = invokeClassLoader; classLoader != null; classLoader = classLoader
            .getParent()) {
            Class clazz = classLoader.getClass();
            if (isBizClassLoader(clazz)) {
                return classLoader;
            }
        }
        return null;
    }

    private boolean isBizClassLoader(Class clazz) {
        if (!ClassLoader.class.isAssignableFrom(clazz)) {
            return false;
        }
        String name = clazz.getName();
        if (Objects.equals(name, bizClassLoaderName)) {
            return true;
        }
        return isBizClassLoader(clazz.getSuperclass());
    }

    private synchronized Properties getWriteProperties() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        if (bizPropertiesMap.containsKey(invokeClassLoader)) {
            return bizPropertiesMap.get(invokeClassLoader);
        }
        ClassLoader classLoader = getBizClassLoader(invokeClassLoader);
        Properties props = classLoader != null
            ? bizPropertiesMap.computeIfAbsent(classLoader, k -> new Properties())
            : baseProperties;
        bizPropertiesMap.put(invokeClassLoader, props);
        return baseProperties;
    }

    private synchronized Set<String> getModifiedKeys() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        if (modifiedKeysMap.containsKey(invokeClassLoader)) {
            return modifiedKeysMap.get(invokeClassLoader);
        }
        ClassLoader classLoader = getBizClassLoader(invokeClassLoader);
        if (classLoader != null) {
            Set<String> keys = modifiedKeysMap.computeIfAbsent(classLoader, k -> new HashSet<>());
            modifiedKeysMap.put(invokeClassLoader, keys);
            return keys;
        }
        return null;
    }

    private void addModifiedKey(String key) {
        addModifiedKeys(Collections.singleton(key));
    }

    private void addModifiedKeys(Collection<String> keys) {
        Set<String> modifiedKeys = getModifiedKeys();
        if (modifiedKeys != null && keys != null) {
            modifiedKeys.addAll(keys);
        }
    }

    /**
     * replace the system properties to multi biz properties
     * if you want to use, you need invoke the method in base application
     *
     * @param bizClassLoaderName a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.common.util.MultiBizProperties} object
     */
    public static MultiBizProperties initSystem(String bizClassLoaderName) {
        Properties properties = System.getProperties();
        if (properties instanceof MultiBizProperties) {
            MultiBizProperties multiBizProperties = (MultiBizProperties) properties;
            if (Objects.equals(multiBizProperties.bizClassLoaderName, bizClassLoaderName)) {
                return multiBizProperties;
            }
        }
        MultiBizProperties multiBizProperties = new MultiBizProperties(bizClassLoaderName,
            properties);
        System.setProperties(multiBizProperties);
        return multiBizProperties;
    }

    /**
     * <p>initSystem.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.common.util.MultiBizProperties} object
     */
    public static MultiBizProperties initSystem() {
        return initSystem(BIZ_CLASS_LOADER);
    }
}
