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

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @since 1.3.0
 */
@Getter
public abstract class AbstractComponent {
    protected String            protocol;
    protected String            identifier;
    protected Object            bean;
    protected Class<?>          beanClass;
    protected Class<?>          interfaceType;

    protected BizRuntimeContext bizRuntimeContext;

    protected Object            metaData;

    public AbstractComponent(String protocol, String identifier, Object bean, Class<?> beanClass,
                             Class<?> interfaceType, Object metaData) {
        this.protocol = protocol;
        this.identifier = identifier;
        this.bean = bean;
        this.beanClass = beanClass;
        this.interfaceType = interfaceType;
        this.metaData = metaData;
    }

    public void setBizRuntimeContext(BizRuntimeContext bizRuntimeContext) {
        this.bizRuntimeContext = bizRuntimeContext;
    }
}