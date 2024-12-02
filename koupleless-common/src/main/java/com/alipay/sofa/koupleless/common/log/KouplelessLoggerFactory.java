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
package com.alipay.sofa.koupleless.common.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessLoggerFactory.java, v 0.1 2024年11月08日 17:31 立蓬 Exp $
 */
public class KouplelessLoggerFactory {
    public static final String     SOFA_KOUPLELESS_LOGGER_SPACE        = "com.alipay.sofa.koupleless";

    private static final String    SOFA_KOUPLELESS_DEFAULT_LOGGER_NAME = "com.alipay.sofa.koupleless";

    public static KouplelessLogger defaultLogger                       = getLogger(
        SOFA_KOUPLELESS_DEFAULT_LOGGER_NAME);

    public static KouplelessLogger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    public static KouplelessLogger getLogger(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return new KouplelessLogger(
            LoggerSpaceManager.getLoggerBySpace(name, SOFA_KOUPLELESS_LOGGER_SPACE));
    }

    public static KouplelessLogger getDefaultLogger() {
        return defaultLogger;
    }
}