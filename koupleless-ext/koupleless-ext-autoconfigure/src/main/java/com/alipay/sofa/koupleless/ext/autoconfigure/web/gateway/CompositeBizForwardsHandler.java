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
package com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway;

import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.SpringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeForwards.java, v 0.1 2024年11月08日 16:00 立蓬 Exp $
 */
public class CompositeBizForwardsHandler implements EventHandler<AbstractArkEvent> {
    private static final Map<ClassLoader, Forwards> forwardsMap = new ConcurrentHashMap<>();

    @Override
    public void handleEvent(AbstractArkEvent event) {
        if (ArkUtils.isMasterBiz()) {
            return;
        }

        // Only handle biz events.
        Object obj = event.getSource();
        if (obj instanceof Biz) {
            Biz biz = (Biz) obj;
            handleBizEvent(event, biz);
        }
    }

    private void handleBizEvent(AbstractArkEvent<Biz> event, Biz biz) {
        // After the module is successfully installed, bean forwards will be automatically registered in the map.
        if (event instanceof AfterBizStartupEvent) {
            Object forwards = SpringUtils.getBean(biz, "forwards");

            if (forwards instanceof Forwards) {
                forwardsMap.put(biz.getBizClassLoader(), (Forwards) forwards);
            }
        }

        // Before the module uninstall, bean forwards will be automatically removed from this map.
        if (event instanceof BeforeBizStopEvent) {
            forwardsMap.remove(biz.getBizClassLoader());
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    public static Map<ClassLoader, Forwards> getBizForwards() {
        return Collections.unmodifiableMap(forwardsMap);
    }
}