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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: SpringApplicationContextAdaptor.java, v 0.1 2024年08月09日 16:17 立蓬 Exp $
 */
public class SpringApplicationContextHolder extends ApplicationContextHolder<ApplicationContext> {
    public SpringApplicationContextHolder(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public <A> Map<String, A> getObjectsOfType(Class<A> type) {
        if (applicationContext instanceof AbstractApplicationContext) {
            ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext) applicationContext)
                .getBeanFactory();
            return beanFactory.getBeansOfType(type);
        }
        return new HashMap<>();
    }

    @Override
    public Object getObject(String key) {
        return applicationContext.getBean(key);
    }

    @Override
    public <A> A getObject(Class<A> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    @Override
    public void close() {
        AbstractApplicationContext ctx = (AbstractApplicationContext) applicationContext;
        // only need shutdown when root context is active
        if (ctx.isActive()) {
            ctx.close();
        }
    }
}