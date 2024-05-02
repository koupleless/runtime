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

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * <p>ArkletLogger class.</p>
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
public class ArkletLogger implements Logger {

    private final Logger logger;

    /**
     * <p>Constructor for ArkletLogger.</p>
     *
     * @param logger a {@link org.slf4j.Logger} object
     */
    public ArkletLogger(Logger logger) {
        this.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return logger.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, format, argArray);
    }

    /** {@inheritDoc} */
    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    /** {@inheritDoc} */
    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    /** {@inheritDoc} */
    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    /** {@inheritDoc} */
    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    /** {@inheritDoc} */
    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    /** {@inheritDoc} */
    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }
}
