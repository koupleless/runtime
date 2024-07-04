/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
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
import org.springframework.context.ApplicationContext;;

import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.koupleless.common.util.SpringUtils.getBean;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ProbeAvailabilityStateIndicator.java, v 0.1 2024年07月04日 14:52 立蓬 Exp $
 */
public class ProbeAvailabilityStateIndicator extends AbstractHealthIndicator{

    private final ApplicationAvailability applicationAvailability;

    public ProbeAvailabilityStateIndicator(ApplicationContext applicationContext){
        super("");
        applicationAvailability = (ApplicationAvailability) applicationContext.getBean("applicationAvailability");
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        builder.withDetail("baseAvailability", BizAvailabilityState.build(applicationAvailability));
        builder.withDetail("bizAvailability",getAllBizAvailabilityState());
        builder.up();
    }

    private Map<String,BizAvailabilityState> getAllBizAvailabilityState(){
        Map<String,BizAvailabilityState> bizAvailabilityStates = new HashMap<>();
        for(Biz biz: ArkClient.getBizManagerService().getBizInOrder()){
            bizAvailabilityStates.put(biz.getIdentity(),getBizAvailabilityState(biz));
        }

        return bizAvailabilityStates;
    }

    private BizAvailabilityState getBizAvailabilityState(Biz biz){
        ApplicationAvailability applicationAvailability = (ApplicationAvailabilityBean) getBean(biz,"applicationAvailability");
        if(null == applicationAvailability){
            return null;
        }
        return BizAvailabilityState.build(applicationAvailability);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    private static class BizAvailabilityState{
        private LivenessState  livenessState;
        private ReadinessState readinessState;

        public static BizAvailabilityState build(ApplicationAvailability applicationAvailability){
            return new BizAvailabilityState(applicationAvailability.getLivenessState(),applicationAvailability.getReadinessState());
        }
    }
}