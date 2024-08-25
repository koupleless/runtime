/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.model;

import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizApplicationContext.java, v 0.1 2024年08月09日 15:44 立蓬 Exp $
 */
public abstract class  BizApplicationContext<T> {
    protected T applicationContext;

    BizApplicationContext(T applicationContext) {
        this.applicationContext = applicationContext;
    }
    final public T get(){
        return this.applicationContext;
    }

    abstract <A> Map<String,A> getObjectsOfType(Class<A> type) throws Exception;

    abstract Object getObject(String key) throws Exception;

    abstract <A> A getObject(Class<A> requiredType) throws Exception;
}