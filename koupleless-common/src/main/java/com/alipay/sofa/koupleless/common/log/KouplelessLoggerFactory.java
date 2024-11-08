/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessLoggerFactory.java, v 0.1 2024年11月08日 17:31 立蓬 Exp $
 */
public class KouplelessLoggerFactory {
    public static final String SOFA_KOUPLELESS_LOGGER_SPACE = "com.alipay.sofa.koupleless";

    private static final String SOFA_KOUPLELESS_DEFAULT_LOGGER_NAME = "com.alipay.sofa.koupleless";

    public static KouplelessLogger defaultLogger = getLogger(SOFA_KOUPLELESS_DEFAULT_LOGGER_NAME);

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
        return new KouplelessLogger(LoggerSpaceManager.getLoggerBySpace(name, SOFA_KOUPLELESS_LOGGER_SPACE));
    }

    public static KouplelessLogger getDefaultLogger() {
        return defaultLogger;
    }
}