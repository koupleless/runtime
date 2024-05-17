/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.service;

import java.util.List;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ComponentRegistry.java, v 0.1 2024年05月17日 15:10 立蓬 Exp $
 */
public interface ComponentRegistry {
    /**
     * Register a component
     *
     * @param bean
     */
    <T extends Component> void register(T bean);

    /**
     * unregister component
     * @param bean
     * @param <T>
     */
    <T extends Component> void unregister(T bean);

    default void register(List<? extends Component> beans) {
        if (beans != null) {
            for (Component bean : beans) {
                register(bean);
            }
        }
    }


}