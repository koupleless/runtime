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
import com.alipay.sofa.ark.spi.model.Biz;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.koupleless.common.util.SpringUtils.getBean;
import static org.springframework.boot.availability.LivenessState.BROKEN;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ProbeAvailabilityStateIndicator.java, v 0.1 2024年07月04日 14:52 立蓬 Exp $
 */
public class ProbeAvailabilityStateIndicator extends AbstractHealthIndicator {

    private final ApplicationAvailability applicationAvailability;

    public ProbeAvailabilityStateIndicator(ApplicationContext applicationContext) {
        super("");
        applicationAvailability = (ApplicationAvailability) applicationContext
            .getBean("applicationAvailability");
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        builder.up();
        builder.withDetail("baseAvailability", BizAvailabilityState
                .build(applicationAvailability));
        builder.withDetail("bizAvailability", getAllBizAvailabilityState());
    }

    private Map<String, BizAvailabilityState> getAllBizAvailabilityState() {
        Map<String, BizAvailabilityState> bizAvailabilityStates = new HashMap<>();
        for (Biz biz : ArkClient.getBizManagerService().getBizInOrder()) {
            bizAvailabilityStates.put(biz.getIdentity(), getBizAvailabilityState(biz));
        }

        return bizAvailabilityStates;
    }

    private BizAvailabilityState getBizAvailabilityState(Biz biz) {
        ApplicationAvailability applicationAvailability;
        try {
            applicationAvailability = (ApplicationAvailabilityBean) getBean(biz,
                "applicationAvailability");
            return BizAvailabilityState.build(applicationAvailability);
        } catch (Exception e) {
            return new BizAvailabilityState(BROKEN, REFUSING_TRAFFIC);
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    private static class BizAvailabilityState {
        private LivenessState  livenessState;
        private ReadinessState readinessState;

        public static BizAvailabilityState build(ApplicationAvailability applicationAvailability) {
            return new BizAvailabilityState(applicationAvailability.getLivenessState(),
                applicationAvailability.getReadinessState());
        }

        public boolean isHealthy() {
            return livenessState == LivenessState.CORRECT
                   && readinessState == ReadinessState.ACCEPTING_TRAFFIC;
        }
    }
}