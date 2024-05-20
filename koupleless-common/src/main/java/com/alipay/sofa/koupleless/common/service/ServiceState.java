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

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ServiceState.java, v 0.1 2024年05月20日 15:22 立蓬 Exp $
 */
public enum ServiceState {
    EXPORTED("exported"), UNEXPORTED("unexported"), BROKEN("broken");

    private String state;

    ServiceState(String state) {
        this.state = state;
    }

    public String getServiceState() {
        return state;
    }

    @Override
    public String toString() {
        return getServiceState();
    }

    public static ServiceState of(String state) {
        for (ServiceState it : values()) {
            if (it.getServiceState().equalsIgnoreCase(state)) {
                return it;
            }
        }
        return BROKEN;
    }
}