/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.model;

import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: SpringApplicationContextAdaptor.java, v 0.1 2024年08月09日 16:17 立蓬 Exp $
 */
public class SpringApplicationContextAdaptor extends BizApplicationContext<ApplicationContext> {
    public SpringApplicationContextAdaptor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public <A> Map<String, A> getObjectsOfType(Class<A> type) {
        return applicationContext.getBeansOfType(type);
    }
    @Override
    public Object getObject(String key) {
        return applicationContext.getBean(key);
    }

    @Override
    public <A> A getObject(Class<A> requiredType){
        return applicationContext.getBean(requiredType);
    }
}