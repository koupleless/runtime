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
package com.alipay.sofa.koupleless.common.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.common.service.ServiceProxyFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.common.util.ArkUtils.checkModuleExists;

/**
 * <p>SpringServiceFinder class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/9/21 9:11 下午
 * @version 1.0.0
 */
public class SpringServiceFinder {

    /**
     * <p>getBaseService.</p>
     *
     * @param name a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    public static <T> T getBaseService(String name, Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.createServiceProxy(masterBiz.getBizName(),
            masterBiz.getBizVersion(), name, serviceType, null);
    }

    /**
     * <p>getBaseService.</p>
     *
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    public static <T> T getBaseService(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.createServiceProxy(masterBiz.getBizName(),
            masterBiz.getBizVersion(), null, serviceType, null);
    }

    /**
     * <p>listBaseServices.</p>
     *
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a {@link java.util.Map} object
     */
    public static <T> Map<String, T> listBaseServices(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.batchCreateServiceProxy(masterBiz.getBizName(),
            masterBiz.getBizVersion(), serviceType, null);
    }

    /**
     * <p>getModuleService.</p>
     *
     * @param moduleName a {@link java.lang.String} object
     * @param moduleVersion a {@link java.lang.String} object
     * @param name a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    public static <T> T getModuleService(String moduleName, String moduleVersion, String name,
                                         Class<T> serviceType) {
        return ServiceProxyFactory.createServiceProxy(moduleName, moduleVersion, name, serviceType,
            null);
    }

    /**
     * 查找所有模块 name 对应的可用服务
     * @param name a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @return a Map<Biz, T> object
     */
    public static <T> Map<Biz, T> getModuleServices(String name, Class<T> serviceType) {
        // 默认不分发，直接查找所有生效的模块中 name 对应的服务
        return getActivatedModuleServices(name, serviceType);
    }

    /**
     * 查找所有生效的模块中 name 对应的服务
     * @param name a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @return a Map<Biz, T> object
     */
    protected static <T> Map<Biz, T> getActivatedModuleServices(String name, Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        List<Biz> bizList = ArkClient.getBizManagerService().getBizInOrder().stream()
            .filter(biz -> biz != masterBiz && biz.getBizState().equals(BizState.ACTIVATED))
            .collect(Collectors.toList());

        Map<Biz, T> bizMap = new HashMap<>();
        for (Biz biz : bizList) {
            // ensure filterServiceProxy called by listAllModuleServices directly，rather than called in the stream()
            T proxy = ServiceProxyFactory.filterServiceProxy(biz.getBizName(), biz.getBizVersion(),
                name, serviceType, null);

            if (proxy != null) {
                bizMap.put(biz, proxy);
            }
        }
        return bizMap;
    }

    /**
     * 找指定模块的 name 对应的可用服务
     * @param moduleName a {@link java.lang.String} object
     * @param serviceName a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @return T
     */
    public static <T> T getModuleServiceWithoutVersion(String moduleName, String serviceName,
                                                       Class<T> serviceType) {
        // 默认不分发，直接查找指定模块中 name 对应的服务。要求模块是已激活状态。
        return getActivatedModuleServiceWithoutVersion(moduleName, serviceName, serviceType);
    }

    /**
     * 找指定模块的 name 对应的服务，如果模块不存在或未激活，则抛出异常；如果模块没有该服务，则抛出异常；
     * @param moduleName a {@link java.lang.String} object
     * @param serviceName a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @return T
     */
    protected static <T> T getActivatedModuleServiceWithoutVersion(String moduleName,
                                                                   String serviceName,
                                                                   Class<T> serviceType) {
        checkModuleExists(moduleName);

        Biz biz = ArkClient.getBizManagerService().getActiveBiz(moduleName);
        if (biz == null) {
            return null;
        }
        return ServiceProxyFactory.createServiceProxy(biz.getBizName(), biz.getBizVersion(),
            serviceName, serviceType, null);
    }

    /**
     * <p>getModuleService.</p>
     *
     * @param moduleName a {@link java.lang.String} object
     * @param moduleVersion a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    public static <T> T getModuleService(String moduleName, String moduleVersion,
                                         Class<T> serviceType) {
        return ServiceProxyFactory.createServiceProxy(moduleName, moduleVersion, null, serviceType,
            null);
    }

    /**
     * <p>listModuleServices.</p>
     *
     * @param moduleName a {@link java.lang.String} object
     * @param moduleVersion a {@link java.lang.String} object
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a {@link java.util.Map} object
     */
    public static <T> Map<String, T> listModuleServices(String moduleName, String moduleVersion,
                                                        Class<T> serviceType) {
        return ServiceProxyFactory.batchCreateServiceProxy(moduleName, moduleVersion, serviceType,
            null);
    }

    /**
     * <p>listAllModuleServices.</p>
     *
     * @param serviceType a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a {@link java.util.Map} object
     */
    public static <T> Map<Biz, Map<String, T>> listAllModuleServices(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        List<Biz> bizList = ArkClient.getBizManagerService().getBizInOrder().stream()
            .filter(biz -> biz != masterBiz).collect(Collectors.toList());

        Map<Biz, Map<String, T>> bizMap = new HashMap<>();
        for (Biz biz : bizList) {
            // ensure batchCreateServiceProxy called by listAllModuleServices directly，rather than called in the stream()
            Map<String, T> proxies = ServiceProxyFactory.batchCreateServiceProxy(biz.getBizName(),
                biz.getBizVersion(), serviceType, null);
            bizMap.put(biz, proxies);
        }

        return bizMap;
    }
}
