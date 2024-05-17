/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BeanRegistry.java, v 0.1 2024年05月17日 14:30 立蓬 Exp $
 */
public class BeanRegistry<T> {
    private Map<String, T> map = new ConcurrentHashMap<>();

    public void register(String key, T bean) {
        map.put(key, bean);
    }


    public void unRegister(String key) {
        map.remove(key);
    }

    public T getBean(String identifier) {
        Object o = map.get(identifier);
        return o == null ? null : (T) o;
    }


    public List<T> getBeans() {
        return new ArrayList<>(map.values());
    }


    public void close() {
        map.clear();
    }
}