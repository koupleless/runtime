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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重写销毁方法 当bean是 来着基座的bean 注册到biz时 不进行销毁
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
     * 当前正在销毁的单例bean
     */
    private static final ThreadLocal<Object> CUR_DESTROY_SINGLE_BEAN_HOLDER = new ThreadLocal<>();

    /**
     * 基座bean复用bean集合
     */
    private static final Set<Object>         BASE_RESUE_BEAN_SET            = Collections
        .newSetFromMap(new ConcurrentHashMap<>());

    private static final String              BIZ_CLASSLOADER                = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

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
        try {
            //复用基座的bean时 记录下复用的基座bean
            if (isSingleton(name) && isOnBiz() && isBaseBeanFactory) {
                BASE_RESUE_BEAN_SET.add(bean);
            }
        } catch (Exception e) {
            logger.error("记录复用基座bean异常", e);
        }
        return bean;
    }

    /**
     * 在模块卸载时 getBeanFactory().destroySingletons();
     * 最终会调用 下面的方法
     * Destroy the given bean. Must destroy beans that depend on the given
     * bean before the bean itself. Should not throw any exceptions.
     *
     * @param beanName the name of the bean
     * @param bean     the bean instance to destroy
     */
    protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
        if (!isBaseBeanFactory && isBaseBean()) {
            //传为null时不进行销毁
            super.destroyBean(beanName, null);
            return;
        }
        super.destroyBean(beanName, bean);

    }

    /**
     * 单例bean销毁是 在线程上下文记录下当前销毁的bean 只在子模块中生效
     *
     * @param beanName the name of the bean
     */
    @Override
    public void destroySingleton(String beanName) {
        if (!isBaseBeanFactory) {
            try {
                Object bean = getBean(beanName);
                CUR_DESTROY_SINGLE_BEAN_HOLDER.set(bean);
                super.destroySingleton(beanName);
            } finally {
                CUR_DESTROY_SINGLE_BEAN_HOLDER.remove();
            }
        } else {
            super.destroySingleton(beanName);
        }
    }

    /**
     * 判断是否是基座的bean
     * 基于记录了那些跨模块的bean
     *
     * @return true 是基座的bean
     */
    private boolean isBaseBean() {
        Object curDestroySingleBean = CUR_DESTROY_SINGLE_BEAN_HOLDER.get();
        if (curDestroySingleBean != null) {
            return BASE_RESUE_BEAN_SET.contains(curDestroySingleBean);
        }
        return false;
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