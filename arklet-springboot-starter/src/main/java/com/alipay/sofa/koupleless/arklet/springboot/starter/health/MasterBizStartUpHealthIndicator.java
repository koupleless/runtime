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
import com.alipay.sofa.ark.spi.event.biz.BizFailedEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MasterBizStartUpHealthIndicator.java, v 0.1 2024年03月21日 10:50 立蓬 Exp $
 */

public class MasterBizStartUpHealthIndicator extends AbstractHealthIndicator
                                                                            implements
                                                                            EventHandler<AbstractArkEvent>,
                                                                            ApplicationListener<SpringApplicationEvent> {
    private Status                                  baseStartUpStatus                     = Status.UNKNOWN;

    private final ConcurrentHashMap<String, Status> bizStartUpStatus                      = new ConcurrentHashMap<>();

    private boolean                                 associateWithArkBizStartUpStatus;

    public static final String                      ASSOCIATE_WITH_ARK_BIZ_STARTUP_STATUS = "koupleless.arklet.health.associateWithArkBizStartUpStatus";

    public MasterBizStartUpHealthIndicator(boolean withBizStartUpStatus) {
        super("ark biz start up health check failed");
        this.associateWithArkBizStartUpStatus = withBizStartUpStatus;

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

        if (event instanceof BeforeBizStartupEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.UNKNOWN);
        } else if (event instanceof AfterBizStartupEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.UP);
        } else if (event instanceof BizFailedEvent) {
            bizStartUpStatus.put(biz.getIdentity(), Status.DOWN);
        } else if (event instanceof AfterBizStopEvent) {
            bizStartUpStatus.remove(biz.getIdentity());
        }
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            baseStartUpStatus = Status.UP;
        } else if (event instanceof ApplicationFailedEvent) {
            baseStartUpStatus = Status.DOWN;
        }
    }

    @Override
    protected void doHealthCheck(Health.Builder builder){
        builder.withDetail(ArkClient.getMasterBiz().getIdentity(),baseStartUpStatus)
                .withDetails(bizStartUpStatus);

        if(!associateWithArkBizStartUpStatus){
            builder.status(baseStartUpStatus);
            return;
        }

        // 健康的情况：基座和模块都启动成功
        if(baseStartUpStatus == Status.UP && bizStartUpStatus.values().stream().allMatch(Status.UP::equals)){
            builder.status(Status.UP);
            return;
        }

        // UNKNOWN 的情况：基座或模块在启动过程中
        if(baseStartUpStatus == Status.UNKNOWN || bizStartUpStatus.values().stream().anyMatch(Status.UNKNOWN::equals)){
            builder.status(Status.UNKNOWN);
            return;
        }

        // 不健康的情况：基座或模块启动失败
        builder.status(Status.DOWN);
    }

    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }
}