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
package com.alipay.sofa.koupleless.ext.web.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>ForwardAutoConfiguration class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
@EnableConfigurationProperties(GatewayProperties.class)
@ComponentScan(basePackages = "com.alipay.sofa.koupleless.ext.web")
public class ForwardAutoConfiguration {
    @Autowired
    private GatewayProperties gatewayProperties;

    /**
     * <p>forwards.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.ext.web.gateway.Forwards} object
     */
    @Bean
    public Forwards forwards() {
        Forwards bean = new Forwards();
        ForwardItems.init(bean, gatewayProperties);
        return bean;
    }
}
