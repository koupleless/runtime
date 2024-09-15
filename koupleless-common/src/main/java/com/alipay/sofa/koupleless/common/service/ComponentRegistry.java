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

import java.util.List;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ComponentRegistry.java, v 0.1 2024年05月17日 15:10 立蓬 Exp $
 */
public interface ComponentRegistry {
    /**
     * Register a component
     *
     * @param bean the component to register
     */
    <T extends AbstractServiceComponent> void registerService(T bean);

    /**
     * unregister component
     * @param bean the component to unregister
     * @param <T> the type of the component
     */
    <T extends AbstractServiceComponent> void unregisterService(T bean);

    <T extends AbstractServiceComponent> T getServiceComponent(String protocol, String identifier);
}
