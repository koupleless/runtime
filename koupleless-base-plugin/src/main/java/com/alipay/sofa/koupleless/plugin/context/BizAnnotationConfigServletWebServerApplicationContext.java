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
package com.alipay.sofa.koupleless.plugin.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.WebApplicationType;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 目的是自定义 beanFactory 的销毁行为
 *
 * @author duanzhiqiang
 * @version BizAnnotationConfigServletWebServerApplicationContext.java, v 0.1 2024年11月08日 16:23 duanzhiqiang
 */
public class BizAnnotationConfigServletWebServerApplicationContext extends
                                                                   AnnotationConfigServletWebServerApplicationContext {
    /**
     * 构造器
     *
     * @param beanFactory 指定的beanFactory
     */
    public BizAnnotationConfigServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return super.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return super.getBean(requiredType, args);
    }

    /**
     * 替换上下文创建行为利用 spring boot factories 扩展 并在默认的优先级前
     * {@link ApplicationContextFactory} registered in {@code spring.factories} to support
     * {@link AnnotationConfigServletWebServerApplicationContext}.
     */
    static class Factory implements ApplicationContextFactory, Ordered {

        @Override
        public Class<? extends ConfigurableEnvironment> getEnvironmentType(WebApplicationType webApplicationType) {
            return (webApplicationType != WebApplicationType.SERVLET) ? null
                : ApplicationServletEnvironment.class;
        }

        @Override
        public ConfigurableEnvironment createEnvironment(WebApplicationType webApplicationType) {
            return (webApplicationType != WebApplicationType.SERVLET) ? null
                : new ApplicationServletEnvironment();
        }

        @Override
        public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
            return (webApplicationType != WebApplicationType.SERVLET) ? null : createContext();
        }

        /**
         * 创建上下文
         *
         * @return 上下文
         */
        private ConfigurableApplicationContext createContext() {
            //自定义BeanFactory的销毁
            DefaultListableBeanFactory beanFactory = new BizDefaultListableBeanFactory();
            return new BizAnnotationConfigServletWebServerApplicationContext(beanFactory);
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }
    }

}