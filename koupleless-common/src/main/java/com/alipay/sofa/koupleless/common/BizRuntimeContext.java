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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.exception.ErrorCodes;
import com.alipay.sofa.koupleless.common.model.ApplicationContextHolder;
import com.alipay.sofa.koupleless.common.model.SpringApplicationContextHolder;
import com.alipay.sofa.koupleless.common.service.AbstractComponent;
import com.alipay.sofa.koupleless.common.service.AbstractServiceComponent;
import com.alipay.sofa.koupleless.common.service.BeanRegistry;
import com.alipay.sofa.koupleless.common.service.ComponentRegistry;
import com.alipay.sofa.koupleless.common.service.ServiceProxyCache;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>BizRuntimeContext class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class BizRuntimeContext implements ComponentRegistry {

    private String                                                               bizName;

    private ClassLoader                                                          appClassLoader;

    private ApplicationContextHolder                                             applicationContext;

    private Map<ClassLoader, Map<String, ServiceProxyCache>>                     serviceProxyCaches = new ConcurrentHashMap<>();

    // Beanregistry key为 "identifier"
    private Map<String/*protocol_name*/, BeanRegistry<AbstractServiceComponent>> serviceMap         = new ConcurrentHashMap<>();

    /**
     * <p>Getter for the field <code>bizName</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getBizName() {
        return bizName;
    }

    /**
     * <p>Setter for the field <code>bizName</code>.</p>
     *
     * @param bizName a {@link java.lang.String} object
     */
    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    /**
     * <p>Getter for the field <code>appClassLoader</code>.</p>
     *
     * @return a {@link java.lang.ClassLoader} object
     */
    public ClassLoader getAppClassLoader() {
        return appClassLoader;
    }

    /**
     * <p>Setter for the field <code>appClassLoader</code>.</p>
     *
     * @param appClassLoader a {@link java.lang.ClassLoader} object
     */
    public void setAppClassLoader(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    /**
     * <p>Getter for the field <code>rootApplicationContext</code>.</p>
     *
     * @return a {@link org.springframework.context.ApplicationContext} object
     */
    @Deprecated
    public ApplicationContext getRootApplicationContext() {
        if (applicationContext == null) {
            return null;
        }

        if (applicationContext.get() instanceof ApplicationContext) {
            return (ApplicationContext) applicationContext.get();
        }

        return null;
    }

    /**
     * <p>Getter for the field <code>applicationContext</code>.</p>
     *
     * @return a {@link ApplicationContextHolder} object
     */
    public ApplicationContextHolder getApplicationContext() {
        return applicationContext;
    }

    /**
     * <p>Setter for the field <code>applicationContext</code>.</p>
     *
     * @param applicationContext a {@link ApplicationContextHolder} object
     */
    public void setApplicationContext(ApplicationContextHolder applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * <p>Setter for the field <code>rootApplicationContext</code>.</p>
     *
     * @param rootApplicationContext a {@link org.springframework.context.ApplicationContext} object
     */
    @Deprecated
    public void setRootApplicationContext(ApplicationContext rootApplicationContext) {
        this.applicationContext = new SpringApplicationContextHolder(rootApplicationContext);
    }

    /**
     * <p>Constructor for BizRuntimeContext.</p>
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     */
    public BizRuntimeContext(Biz biz) {
        this(biz, null);
    }

    /**
     * <p>Constructor for BizRuntimeContext.</p>
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     * @param applicationContext a {@link org.springframework.context.ApplicationContext} object
     */
    public BizRuntimeContext(Biz biz, ApplicationContext applicationContext) {
        this.bizName = biz.getBizName();
        this.appClassLoader = biz.getBizClassLoader();

        if (applicationContext != null) {
            this.applicationContext = new SpringApplicationContextHolder(applicationContext);
        }
    }

    /**
     * <p>Getter for the field <code>serviceProxyCaches</code>.</p>
     *
     * @return a {@link java.util.Map} object
     */
    public Map<ClassLoader, Map<String, ServiceProxyCache>> getServiceProxyCaches() {
        return serviceProxyCaches;
    }

    /**
     * <p>removeServiceProxyCaches.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object
     */
    public void removeServiceProxyCaches(ClassLoader classLoader) {
        serviceProxyCaches.remove(classLoader);
    }

    /**
     * 方法名为 shutdown() 会导致卸载时候调用两次
     */
    public void shutdownContext() {
        try {
            applicationContext.close();
            appClassLoader = null;
        } catch (Throwable throwable) {
            throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100001, throwable);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerService(AbstractServiceComponent bean) {
        bean.setBizRuntimeContext(this);
        serviceMap.putIfAbsent(bean.getProtocol(), new BeanRegistry<>());
        doRegister(serviceMap.get(bean.getProtocol()), bean);
    }

    private void doRegister(BeanRegistry registry, AbstractComponent bean) {
        registry.register(bean.getIdentifier(), bean);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterService(AbstractServiceComponent bean) {
        BeanRegistry<AbstractServiceComponent> registry = serviceMap.get(bean.getProtocol());

        if (null == registry) {
            throw new BizRuntimeException(ErrorCodes.ServiceManager.E200002,
                "protocol service" + bean.getProtocol() + " has not registered");
        }

        registry.unRegister(bean.getIdentifier());
    }

    /** {@inheritDoc} */
    @Override
    public <T extends AbstractServiceComponent> T getServiceComponent(String protocol,
                                                                      String identifier) {
        if (serviceMap.get(protocol) == null) {
            return null;
        }
        AbstractComponent serviceComponent = serviceMap.get(protocol).getBean(identifier);
        return serviceComponent == null ? null : (T) serviceComponent;
    }
}
