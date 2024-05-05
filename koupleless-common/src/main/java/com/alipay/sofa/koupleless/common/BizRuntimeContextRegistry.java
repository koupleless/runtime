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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.exception.ErrorCodes;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>BizRuntimeContextRegistry class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class BizRuntimeContextRegistry {
    private static ConcurrentHashMap<ClassLoader, BizRuntimeContext> contextMap = new ConcurrentHashMap<>();

    /**
     * <p>registerBizRuntimeManager.</p>
     *
     * @param bizRuntimeContext a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static void registerBizRuntimeManager(BizRuntimeContext bizRuntimeContext) {
        contextMap.put(bizRuntimeContext.getAppClassLoader(), bizRuntimeContext);
    }

    /**
     * <p>unRegisterBizRuntimeManager.</p>
     *
     * @param bizRuntimeContext a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static void unRegisterBizRuntimeManager(BizRuntimeContext bizRuntimeContext) {
        contextMap.remove(bizRuntimeContext.getAppClassLoader());
    }

    /**
     * <p>getRuntimeSet.</p>
     *
     * @return a {@link java.util.Set} object
     */
    public static Set<BizRuntimeContext> getRuntimeSet() {
        return Collections.unmodifiableSet(new CopyOnWriteArraySet<>(contextMap.values()));
    }

    /**
     * <p>getRuntimeMap.</p>
     *
     * @return a {@link java.util.concurrent.ConcurrentHashMap} object
     */
    public static ConcurrentHashMap<ClassLoader, BizRuntimeContext> getRuntimeMap() {
        return contextMap;
    }

    /**
     * 获取 biz 对应的 SofaRuntimeManager
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     * @return a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static BizRuntimeContext getBizRuntimeContext(Biz biz) {
        if (BizRuntimeContextRegistry.getRuntimeMap().containsKey(biz.getBizClassLoader())) {
            return BizRuntimeContextRegistry.getRuntimeMap().get(biz.getBizClassLoader());
        }

        throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
            "No BizRuntimeContext found for biz: " + biz.getBizName());
    }

    /**
     * <p>getMasterBizRuntimeContext.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static BizRuntimeContext getMasterBizRuntimeContext() {
        Biz masterBiz = ArkClient.getMasterBiz();
        return getBizRuntimeContext(masterBiz);
    }

    /**
     * <p>getBizRuntimeContextByClassLoader.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object
     * @return a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static BizRuntimeContext getBizRuntimeContextByClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
                "Can't find BizRuntimeContext for null classLoader");
        }

        if (BizRuntimeContextRegistry.getRuntimeMap().containsKey(classLoader)) {
            return BizRuntimeContextRegistry.getRuntimeMap().get(classLoader);
        }

        throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
            "No BizRuntimeContext found for classLoader: " + classLoader);
    }

    /**
     * <p>getBizRuntimeContextByApplicationContext.</p>
     *
     * @param applicationContext a {@link org.springframework.context.ApplicationContext} object
     * @return a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    public static BizRuntimeContext getBizRuntimeContextByApplicationContext(ApplicationContext applicationContext) {
        for (BizRuntimeContext bizRuntimeContext : BizRuntimeContextRegistry.getRuntimeSet()) {
            if (bizRuntimeContext.getRootApplicationContext() != null
                && bizRuntimeContext.getRootApplicationContext().equals(applicationContext)) {
                return bizRuntimeContext;
            }
        }

        throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
            "No BizRuntimeContext found for applicationContext: " + applicationContext);
    }
}
