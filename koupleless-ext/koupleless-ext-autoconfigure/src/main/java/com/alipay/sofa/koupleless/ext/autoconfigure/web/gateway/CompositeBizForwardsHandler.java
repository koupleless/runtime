/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway;

import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.SpringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeForwards.java, v 0.1 2024年11月08日 16:00 立蓬 Exp $
 */
public class CompositeBizForwardsHandler implements EventHandler<AbstractArkEvent> {
     private static final Map<Biz,Forwards> forwardsMap = new HashMap<>();

    @Override
    public void handleEvent(AbstractArkEvent event) {
        if(ArkUtils.isMasterBiz()){
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
        if(event instanceof AfterBizStartupEvent) {
            Object forwards = SpringUtils.getBean(biz, "forwards");

            if(forwards instanceof Forwards) {
                forwardsMap.put(biz, (Forwards) forwards);
            }
        }

        // Before the module uninstall, bean forwards will be automatically removed from this map.
        if(event instanceof BeforeBizStopEvent) {
            forwardsMap.remove(biz);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    public static Map<Biz,Forwards> getBizForwards() {
        return forwardsMap;
    }
}