/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.service;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: Component.java, v 0.1 2024年05月17日 14:26 立蓬 Exp $
 */
public interface Component {
    /**
     * Get protocol of service
     *
     * @return
     */
    String getProtocol();

    /**
     * Get service identifier to export
     *
     * @return
     */
    String getIdentifier();

    /**
     * Get real implement bean
     *
     * @return
     */
    Object getBean();

    /**
     * Get the class of bean
     *
     * @return
     */
    Class<?> getBeanClass();

    /**
     * Get interface class
     *
     * @return
     */
    Class<?> getInterface();

    /**
     * Get module context of component
     *
     * @return
     */
    BizRuntimeContext getBizRuntimeContext();

    /**
     * Set module context of component
     *
     * @param context
     */
    void setBizRuntimeContext(BizRuntimeContext context);
}