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

import java.util.Map;

/**
 * <p>Abstract ApplicationContextHolder class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizApplicationContext.java, v 0.1 2024年08月09日 15:44 立蓬 Exp $
 * @since 1.3.1
 */
public abstract class ApplicationContextHolder<T> {
    protected T applicationContext;

    ApplicationContextHolder(T applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * <p>get.</p>
     *
     * @return a T object
     */
    final public T get() {
        return this.applicationContext;
    }

    /**
     * <p>getObjectsOfType.</p>
     *
     * @param type a {@link java.lang.Class} object
     * @param <A> a A class
     * @return a {@link java.util.Map} object
     */
    public abstract <A> Map<String, A> getObjectsOfType(Class<A> type);

    /**
     * <p>getObject.</p>
     *
     * @param key a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     */
    public abstract Object getObject(String key);

    /**
     * <p>getObject.</p>
     *
     * @param requiredType a {@link java.lang.Class} object
     * @param <A> a A class
     * @return a A object
     */
    public abstract <A> A getObject(Class<A> requiredType);

    /**
     * <p>close.</p>
     */
    public abstract void close();
}
