/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.model;

import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MainBizApplicationContext.java, v 0.1 2024年08月25日 23:35 立蓬 Exp $
 */
public class MainBizApplicationContext extends BizApplicationContext<MainApplicationContext>{
    MainBizApplicationContext(MainApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    <A> Map<String, A> getObjectsOfType(Class<A> type) throws Exception {
        return applicationContext.getObjectMap(type);
    }

    @Override
    Object getObject(String key) throws Exception {
        return applicationContext.getObject(key);
    }

    @Override
    <A> A getObject(Class<A> requiredType) throws Exception {
        Map<String,A> objMap = applicationContext.getObjectMap(requiredType);
        if(objMap.isEmpty()){
            return null;
        }
        if(objMap.size() > 1){
            throw new RuntimeException("more than one object of type " + requiredType.getName());
        }
        return objMap.values().stream().findFirst().get();
    }
}