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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizFailedEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基座启动健康指标，可以通过 koupleless.healthcheck.base.readiness.withAllBizReadiness 配置该健康指标是否和模块启动健康指标关联，默认为 false，即不关联。
 * 如：为 true 时，如果模块安装失败，则基座启动健康指标为 DOWN；
 * 如：为 false 时，无论模块安装成功或失败，基座启动健康指标仅和基座启动成功相关。
 * 注意：后续做主动基座回放时，可以参考该类实现。
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BaseStartUpHealthIndicator.java, v 0.1 2024年03月21日 10:50 立蓬 Exp $
 * @since 1.1.0
 */
public class BaseStartUpHealthIndicator extends AbstractHealthIndicator
                                        implements EventHandler<AbstractArkEvent>,
                                        ApplicationListener<SpringApplicationEvent> {
    private Status                                  baseStartUpStatus      = Status.UNKNOWN;

    private final ConcurrentHashMap<String, Status> bizStartUpStatus       = new ConcurrentHashMap<>();

    private boolean                                 associateWithAllBizReadiness;

    /** Constant <code>WITH_ALL_BIZ_READINESS="koupleless.healthcheck.base.readiness.w"{trunked}</code> */
    public static final String                      WITH_ALL_BIZ_READINESS = "koupleless.healthcheck.base.readiness.withAllBizReadiness";

    public Method                                   healthBuildWithDetails = null;

    /**
     * <p>Constructor for BaseStartUpHealthIndicator.</p>
     *
     * @param withAllBizReadiness a boolean
     */
    public BaseStartUpHealthIndicator(boolean withAllBizReadiness) {

        Constructor<?>[] constructors = this.getClass().getSuperclass().getConstructors();
        // check whether contains constructor with String parameter
        boolean found = false;
        Constructor<?> constructorWithStringArg = null;
        Constructor<?> constructorWithNoArg = null;
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == 1 && c.getParameters()[0].getType().equals(String.class)) {
                constructorWithStringArg = c;
            } else if (c.getParameterCount() == 0) {
                constructorWithNoArg = c;
            }
        }

        try {
            if (constructorWithStringArg != null) {
                constructorWithStringArg.newInstance("ark biz start up health check failed");
            } else if (constructorWithNoArg != null) {
                constructorWithNoArg.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // ignore
            throw new RuntimeException("failed to create base startup health indicator", e);
        }

        try {
            this.healthBuildWithDetails = Health.Builder.class.getMethod("withDetails", Map.class);
        } catch (NoSuchMethodException e) {
            // ignore
        }

        this.associateWithAllBizReadiness = withAllBizReadiness;
    }

    /** {@inheritDoc} */
    @Override
    public void handleEvent(AbstractArkEvent event) {
        Object obj = event.getSource();
        if (obj instanceof Biz) {
            Biz biz = (Biz) obj;
            handleBizEvent(event, biz);
        }
    }

    private void handleBizEvent(AbstractArkEvent<Biz> event, Biz biz) {
        if (biz == ArkClient.getMasterBiz()) {
            return;
        }

        if (event instanceof BeforeBizStartupEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.UNKNOWN);
        } else if (event instanceof AfterBizStartupEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.UP);
        } else if (event instanceof AfterBizFailedEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.DOWN);
        } else if (event instanceof AfterBizStopEvent) {
            bizStartUpStatus.remove(biz.getIdentity());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            baseStartUpStatus = Status.UP;
        } else if (event instanceof ApplicationFailedEvent) {
            baseStartUpStatus = Status.DOWN;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        builder.withDetail(ArkClient.getMasterBiz().getIdentity(), baseStartUpStatus);

        if (healthBuildWithDetails != null) {
            try {
                healthBuildWithDetails.invoke(builder, bizStartUpStatus);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!associateWithAllBizReadiness) {
            builder.status(baseStartUpStatus);
            return;
        }

        // 健康的情况：基座和模块都启动成功
        if (baseStartUpStatus == Status.UP
            && bizStartUpStatus.values().stream().allMatch(Status.UP::equals)) {
            builder.status(Status.UP);
            return;
        }

        // UNKNOWN 的情况：基座或模块在启动过程中
        if (baseStartUpStatus == Status.UNKNOWN
            || bizStartUpStatus.values().stream().anyMatch(Status.UNKNOWN::equals)) {
            builder.status(Status.UNKNOWN);
            return;
        }

        // 不健康的情况：基座或模块启动失败
        builder.status(Status.DOWN);
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }
}
