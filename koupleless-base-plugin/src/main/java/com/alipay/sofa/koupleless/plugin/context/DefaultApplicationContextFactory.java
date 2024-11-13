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

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;

/**
 * WebApplicationType.NONE 自定义上下文
 * 1、基于Spring Boot  ApplicationContextFactory  SPI扩展 当WebApplicationType.NONE创建一个上下文
 * 2、仅处理WebApplicationType.NONE 当返回为null时  有其他SPI扩展去处理 即其他ApplicationContextFactory去处理 不同的WebApplicationType
 * 3、指定 BizDefaultListableBeanFactory{@link com.alipay.sofa.koupleless.plugin.context.BizDefaultListableBeanFactory} 来自定义子模块的销毁行为
 * <p>
 * Custom ApplicationContextFactory for WebApplicationType.NONE that uses BizDefaultListableBeanFactory
 * to prevent destruction of base singleton beans when a module is destroyed.
 * This factory has the highest precedence and only handles non-web application contexts.
 *
 * @author duanzhiqiang
 * @version DefaultApplicationContextFactory.java, v 0.1 2024年11月12日 17:55 duanzhiqiang
 */
public class DefaultApplicationContextFactory implements ApplicationContextFactory, Ordered {
    @Override
    public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
        return (webApplicationType != WebApplicationType.NONE) ? null
            : new AnnotationConfigApplicationContext(new BizDefaultListableBeanFactory());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}