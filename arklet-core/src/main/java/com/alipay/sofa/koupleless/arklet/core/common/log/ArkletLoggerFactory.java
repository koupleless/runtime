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
package com.alipay.sofa.koupleless.arklet.core.common.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;

/**
 * <p>ArkletLoggerFactory class.</p>
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
public class ArkletLoggerFactory {

    /** Constant <code>ARKLET_LOGGER_SPACE="com.alipay.sofa.arklet"</code> */
    public static final String  ARKLET_LOGGER_SPACE        = "com.alipay.sofa.arklet";

    private static final String ARKLET_DEFAULT_LOGGER_NAME = "com.alipay.sofa.arklet";

    /** Constant <code>defaultLogger</code> */
    public static ArkletLogger  defaultLogger;

    /**
     * <p>getLogger.</p>
     *
     * @param clazz a {@link java.lang.Class} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger} object
     */
    public static ArkletLogger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    /**
     * <p>getLogger.</p>
     *
     * @param name a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger} object
     */
    public static ArkletLogger getLogger(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return new ArkletLogger(LoggerSpaceManager.getLoggerBySpace(name, ARKLET_LOGGER_SPACE));
    }

    /**
     * <p>Getter for the field <code>defaultLogger</code>.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger} object
     */
    public static ArkletLogger getDefaultLogger() {
        if (defaultLogger == null) {
            defaultLogger = getLogger(ARKLET_DEFAULT_LOGGER_NAME);
        }
        return defaultLogger;
    }

}
