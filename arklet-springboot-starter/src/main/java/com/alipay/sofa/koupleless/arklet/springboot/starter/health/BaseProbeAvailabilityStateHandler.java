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
import com.alipay.sofa.ark.spi.event.biz.AfterBizFailedEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CopyOnWriteArraySet;

import static com.alipay.sofa.koupleless.common.util.SpringUtils.getBean;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BaseProbeAvailibity.java, v 0.1 2024年07月04日 12:56 立蓬 Exp $
 */
public class BaseProbeAvailabilityStateHandler implements EventHandler<AbstractArkEvent> {

    private final CopyOnWriteArraySet<Biz> startingBizSet = new CopyOnWriteArraySet<>();

    private boolean                        associateWithAllBizReadiness;

    private ApplicationContext             baseContext;
    private final ApplicationAvailability  applicationAvailability;

    public BaseProbeAvailabilityStateHandler(ApplicationContext applicationContext,
                                             boolean withAllBizReadiness) {
        baseContext = applicationContext;
        associateWithAllBizReadiness = withAllBizReadiness;
        applicationAvailability = (ApplicationAvailability) baseContext
            .getBean("applicationAvailability");
    }

    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }

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

        if (!associateWithAllBizReadiness) {
            return;
        }

        // 模块启动前，关闭流量，添加该模块
        if (event instanceof BeforeBizStartupEvent) {
            AvailabilityChangeEvent.publish(baseContext, ReadinessState.REFUSING_TRAFFIC);
            startingBizSet.add(biz);
            return;
        }

        // 模块启动失败，保持流量关闭
        if (event instanceof AfterBizFailedEvent) {
            AvailabilityChangeEvent.publish(baseContext, ReadinessState.REFUSING_TRAFFIC);
            return;
        }

        // 模块启动后，如果基座本身 ready 且所有正在启动的模块都 ready，那么开启流量
        if (event instanceof AfterBizStartupEvent) {
            if (applicationAvailability.getLivenessState() == LivenessState.CORRECT
                && getCompositeStartingBizReadiness() == ReadinessState.ACCEPTING_TRAFFIC) {
                AvailabilityChangeEvent.publish(baseContext, ReadinessState.ACCEPTING_TRAFFIC);
            }
            return;
        }

        // 模块卸载前：关闭流量
        if (event instanceof BeforeBizStopEvent) {
            AvailabilityChangeEvent.publish(baseContext, ReadinessState.REFUSING_TRAFFIC);
            return;
        }

        // 模块卸载后，移除该模块
        if (event instanceof AfterBizStopEvent) {
            startingBizSet.remove(biz);
        }
    }

    private ReadinessState getCompositeStartingBizReadiness() {
        if (startingBizSet.stream()
            .allMatch(biz -> getReadinessState(biz) == ReadinessState.ACCEPTING_TRAFFIC)) {
            return ReadinessState.ACCEPTING_TRAFFIC;
        }
        return ReadinessState.REFUSING_TRAFFIC;
    }

    private ReadinessState getReadinessState(Biz biz) {
        try {
            ApplicationAvailabilityBean applicationAvailability = (ApplicationAvailabilityBean) getBean(
                biz, "applicationAvailability");
            return applicationAvailability.getReadinessState();
        } catch (Exception e) {
            return ReadinessState.REFUSING_TRAFFIC;
        }
    }
}