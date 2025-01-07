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
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1、当模块获取基座单例时bean 记录引用（由于无法通过名称获取bean 因为在子模块中 beanName是可以重新定义的）
 * 2、在模块创建这个复用的bean时 不注册销毁行为  支持复用bean 注册单例或者是其他scope
 * 3、通过记录的复用的bean 引用来判断是基座复用bean
 *
 * @author duanzhiqiang
 * @version BizDefaultListableBeanFactory.java, v 0.1 2024年11月08日 16:45 duanzhiqiang
 */
public class BizDefaultListableBeanFactory extends DefaultListableBeanFactory {

    /**
     * 是否是基座beanFactory
     */
    private final boolean isBaseBeanFactory;

    /**
     * 在创建时 额外判断是否是基座bean
     */
    public BizDefaultListableBeanFactory() {
        super();
        this.isBaseBeanFactory = !isOnBiz();
    }

    /**
     * 基座bean复用bean集合引用
     */
    private static final Set<Object> BASE_FACTORY_REUSE_BEAN_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 模块的类加载器名
     */
    private static final String BIZ_CLASSLOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    /**
     * 在子模块获取 base 的bean 记录这个bean 引用
     *
     * @param name          the name of the bean to retrieve
     * @param requiredType  the required type of the bean to retrieve
     * @param args          arguments to use when creating a bean instance using explicit arguments
     *                      (only applied when creating a new instance as opposed to retrieving an existing one)
     * @param typeCheckOnly whether the instance is obtained for a type check,
     *                      not for actual use
     * @param <T>           the required type of the bean to retrieve
     * @return bean
     * @throws BeansException 异常
     */
    @Override
    protected <T> T doGetBean(String name, Class<T> requiredType, Object[] args,
                              boolean typeCheckOnly) throws BeansException {

        T bean = super.doGetBean(name, requiredType, args, typeCheckOnly);

        // 只有是基座isBaseBeanFactory 但获取bean时是模块发起调用（即复用基座的bean时） 记录下复用的基座bean
        if (isBaseBeanFactory && isOnBiz() && isSingleton(name)) {
            BASE_FACTORY_REUSE_BEAN_SET.add(bean);
        }

        return bean;
    }

    @Override
    public void destroySingletons() {
        super.destroySingletons();
        //复用bean在 基座销毁时清空
        if (isBaseBeanFactory) {
            BASE_FACTORY_REUSE_BEAN_SET.clear();
        }
    }

    /**
     * 判断是否需要销毁
     * 扩展如果是模块上下文且是基座复用的bean 则不需要进行销毁
     *
     * @param bean the bean instance to check
     * @param mbd  the corresponding bean definition
     * @return false 不需要销毁 true 需要销毁
     */
    @Override
    protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
        //如果是模块上下文且是基座复用的bean 则不需要进行销毁
        if (!isBaseBeanFactory && isBaseReuseBean(bean)) {
            //不注册DisposableBean
            return false;
        }
        return super.requiresDestruction(bean, mbd);
    }

    /**
     * 是否是基座复用的bean
     *
     * @param bean bean 实例
     * @return true 是 false 不是
     */
    private boolean isBaseReuseBean(Object bean) {
        return BASE_FACTORY_REUSE_BEAN_SET.contains(bean);
    }

    /**
     * 判断是否在biz中 而不是基座中
     *
     * @return true 在biz中
     */
    private boolean isOnBiz() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader == null
                || BIZ_CLASSLOADER.equals(contextClassLoader.getClass().getName());
    }

    /**
     * Getter method for property <tt>isBase</tt>.
     *
     * @return property value of isBase
     */
    public boolean isBaseBeanFactory() {
        return isBaseBeanFactory;
    }

}