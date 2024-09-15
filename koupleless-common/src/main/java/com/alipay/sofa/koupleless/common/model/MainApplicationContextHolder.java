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

import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100007;

/**
 * <p>MainApplicationContextHolder class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MainBizApplicationContext.java, v 0.1 2024年08月25日 23:35 立蓬 Exp $
 * @since 1.3.1
 */
public class MainApplicationContextHolder extends ApplicationContextHolder<MainApplicationContext> {
    /**
     * <p>Constructor for MainApplicationContextHolder.</p>
     *
     * @param applicationContext a {@link com.alipay.sofa.koupleless.common.model.MainApplicationContext} object
     */
    public MainApplicationContextHolder(MainApplicationContext applicationContext) {
        super(applicationContext);
    }

    /** {@inheritDoc} */
    @Override
    public <A> Map<String, A> getObjectsOfType(Class<A> type) {
        return applicationContext.getObjectMap(type);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String key) {
        if (applicationContext.getObject(key) == null) {
            throw new BizRuntimeException(E100007, "object not found: " + key);
        }
        return applicationContext.getObject(key);
    }

    /** {@inheritDoc} */
    @Override
    public <A> A getObject(Class<A> requiredType) {
        Map<String, A> objMap = applicationContext.getObjectMap(requiredType);
        if (objMap.isEmpty()) {
            return null;
        }
        Set<A> values = new HashSet<>(objMap.values());
        if (values.size() > 1) {
            throw new RuntimeException("more than one object of type " + requiredType.getName());
        }
        return objMap.values().stream().findFirst().get();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        applicationContext.close();
    }
}
