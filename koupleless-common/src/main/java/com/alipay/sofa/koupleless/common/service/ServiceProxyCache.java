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

/**
 * <p>ServiceProxyCache class.</p>
 *
 * @author: yuanyuan
 * @date: 2023/9/25 11:37 下午
 * @author zzl_i
 * @version 1.0.0
 */
public class ServiceProxyCache {

    private Object               proxy;

    private SpringServiceInvoker invoker;

    /**
     * <p>Constructor for ServiceProxyCache.</p>
     *
     * @param proxy a {@link java.lang.Object} object
     * @param invoker a {@link com.alipay.sofa.koupleless.common.service.SpringServiceInvoker} object
     */
    public ServiceProxyCache(Object proxy, SpringServiceInvoker invoker) {
        this.proxy = proxy;
        this.invoker = invoker;
    }

    /**
     * <p>Getter for the field <code>proxy</code>.</p>
     *
     * @return a {@link java.lang.Object} object
     */
    public Object getProxy() {
        return proxy;
    }

    /**
     * <p>Setter for the field <code>proxy</code>.</p>
     *
     * @param proxy a {@link java.lang.Object} object
     */
    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    /**
     * <p>Getter for the field <code>invoker</code>.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.common.service.SpringServiceInvoker} object
     */
    public SpringServiceInvoker getInvoker() {
        return invoker;
    }

    /**
     * <p>Setter for the field <code>invoker</code>.</p>
     *
     * @param invoker a {@link com.alipay.sofa.koupleless.common.service.SpringServiceInvoker} object
     */
    public void setInvoker(SpringServiceInvoker invoker) {
        this.invoker = invoker;
    }
}
