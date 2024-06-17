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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>BeanRegistry class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BeanRegistry.java, v 0.1 2024年05月17日 14:30 立蓬 Exp $
 */
public class BeanRegistry<T> {
    private Map<String, T> map = new ConcurrentHashMap<>();

    /**
     * <p>register.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param bean a T object
     */
    public void register(String key, T bean) {
        map.put(key, bean);
    }

    /**
     * <p>unRegister.</p>
     *
     * @param key a {@link java.lang.String} object
     */
    public void unRegister(String key) {
        map.remove(key);
    }

    /**
     * <p>getBean.</p>
     *
     * @param identifier a {@link java.lang.String} object
     * @return a T object
     */
    public T getBean(String identifier) {
        Object o = map.get(identifier);
        return o == null ? null : (T) o;
    }

    /**
     * <p>getBeans.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<T> getBeans() {
        return new ArrayList<>(map.values());
    }

    /**
     * <p>close.</p>
     */
    public void close() {
        map.clear();
    }
}
