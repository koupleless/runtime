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
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;

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
}
