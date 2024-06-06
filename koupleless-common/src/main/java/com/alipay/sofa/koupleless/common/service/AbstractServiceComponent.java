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
package com.alipay.sofa.koupleless.common.service;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>Abstract AbstractServiceComponent class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @since 1.3.0
 * @version $Id: $Id
 */
public abstract class AbstractServiceComponent extends AbstractComponent {
    @Setter
    @Getter
    private ServiceState serviceState;

    /**
     * <p>Constructor for AbstractServiceComponent.</p>
     *
     * @param protocol a {@link java.lang.String} object
     * @param identifier a {@link java.lang.String} object
     * @param bean a {@link java.lang.Object} object
     * @param beanClass a {@link java.lang.Class} object
     * @param interfaceType a {@link java.lang.Class} object
     * @param metaData a {@link java.lang.Object} object
     * @param serviceState a {@link com.alipay.sofa.koupleless.common.service.ServiceState} object
     */
    public AbstractServiceComponent(String protocol, String identifier, Object bean,
                                    Class<?> beanClass, Class<?> interfaceType, Object metaData,
                                    ServiceState serviceState) {
        super(protocol, identifier, bean, beanClass, interfaceType, metaData);
        this.serviceState = serviceState;
    }
}
