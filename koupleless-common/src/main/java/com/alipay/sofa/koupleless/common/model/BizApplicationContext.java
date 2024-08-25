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
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizApplicationContext.java, v 0.1 2024年08月09日 15:44 立蓬 Exp $
 */
public abstract class BizApplicationContext<T> {
    protected T applicationContext;

    BizApplicationContext(T applicationContext) {
        this.applicationContext = applicationContext;
    }

    final public T get() {
        return this.applicationContext;
    }

    public abstract <A> Map<String, A> getObjectsOfType(Class<A> type);

    public abstract Object getObject(String key);

    public abstract <A> A getObject(Class<A> requiredType);

    public abstract void close();
}