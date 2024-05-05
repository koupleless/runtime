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
package com.alipay.sofa.koupleless.arklet.core.command.meta;

import java.lang.reflect.ParameterizedType;

import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;

/**
 * <p>Abstract AbstractCommandHandler class.</p>
 *
 * @author mingmen
 * @since 2023/6/8
 * @version 1.0.0
 */

@SuppressWarnings("unchecked")
public abstract class AbstractCommandHandler<P extends InputMeta, Q> {

    private final UnifiedOperationService unifiedOperationService = ArkletComponentRegistry
        .getOperationServiceInstance();
    private final CommandService          commandService          = ArkletComponentRegistry
        .getCommandServiceInstance();
    private final HealthService           healthService           = ArkletComponentRegistry
        .getHealthServiceInstance();

    /**
     * <p>validate.</p>
     *
     * @param p a P object
     * @throws com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException if any.
     */
    public abstract void validate(P p) throws CommandValidationException;

    /**
     * <p>handle.</p>
     *
     * @param p a P object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Output} object
     */
    public abstract Output<Q> handle(P p);

    /**
     * <p>command.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Command} object
     */
    public abstract Command command();

    /**
     * <p>getOperationService.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService} object
     */
    public UnifiedOperationService getOperationService() {
        return unifiedOperationService;
    }

    /**
     * <p>Getter for the field <code>commandService</code>.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.CommandService} object
     */
    public CommandService getCommandService() {
        return commandService;
    }

    /**
     * <p>Getter for the field <code>healthService</code>.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.health.HealthService} object
     */
    public HealthService getHealthService() {
        return healthService;
    }

    /**
     * <p>getInputClass.</p>
     *
     * @return a {@link java.lang.Class} object
     */
    public Class<P> getInputClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<P>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * <p>isTrue.</p>
     *
     * @param expression a boolean
     * @param message a {@link java.lang.String} object
     * @param values a {@link java.lang.Object} object
     */
    public static void isTrue(final boolean expression, final String message,
                              final Object... values) {
        if (!expression) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    /**
     * <p>notBlank.</p>
     *
     * @param check a {@link java.lang.String} object
     * @param message a {@link java.lang.String} object
     * @param values a {@link java.lang.Object} object
     */
    public static void notBlank(final String check, final String message, final Object... values) {
        if (StringUtil.isBlank(check)) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    /**
     * <p>notNull.</p>
     *
     * @param check a {@link java.lang.Object} object
     * @param message a {@link java.lang.String} object
     * @param values a {@link java.lang.Object} object
     */
    public static void notNull(final Object check, final String message, final Object... values) {
        if (null == check) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

}
