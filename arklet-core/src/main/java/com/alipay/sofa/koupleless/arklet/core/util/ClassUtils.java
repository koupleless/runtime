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
package com.alipay.sofa.koupleless.arklet.core.util;

import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>ClassUtils class.</p>
 *
 * @author dongnan
 * @version 1.0.0
 */
public class ClassUtils {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    /**
     * Validate current object must be null
     *
     * @param customTunnelClassName String custom tunnel class name
     */
    public static Class<? extends Tunnel> getCustomTunnelClass(String customTunnelClassName) throws ArkletInitException {
        Class<?> tunnelClass;
        try {
            tunnelClass = Class.forName(customTunnelClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("custom tunnel class not found");
            throw new ArkletInitException("custom tunnel class not found", e);
        }
        if (!Tunnel.class.isAssignableFrom(tunnelClass)) {
            LOGGER.error("custom tunnel class didn't implement tunnel interface");
            throw new ArkletInitException("custom tunnel class didn't implement tunnel interface");
        }
        return (Class<? extends Tunnel>) tunnelClass;
    }

    /**
     * Validate current object must be null
     *
     * @param baseMetadataHookClassName String custom base metadata hook class name
     */
    public static BaseMetadataHook getBaseMetadataHookImpl(String baseMetadataHookClassName) throws ArkletInitException {
        Class<?> hookClass;
        try {
            hookClass = Class.forName(baseMetadataHookClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("custom base metadata hook class not found");
            throw new ArkletInitException("custom base metadata hook class not found", e);
        }
        if (!BaseMetadataHook.class.isAssignableFrom(hookClass)) {
            LOGGER.error("custom base metadata hook class didn't implement Metadata interface");
            throw new ArkletInitException(
                "custom base metadata hook class didn't implement tunnel interface");
        }
        BaseMetadataHook hookInstance;
        try {
            hookInstance = (BaseMetadataHook) hookClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.error("Failed to instantiate the custom base metadata hook class", e);
            throw new ArkletInitException(
                "Failed to instantiate the custom base metadata hook class", e);
        }

        return hookInstance;
    }
}
