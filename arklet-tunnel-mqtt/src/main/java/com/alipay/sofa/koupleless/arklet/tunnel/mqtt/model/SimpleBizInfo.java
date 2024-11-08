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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model;

import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;

import java.io.Serializable;

/**
 * <p>SimpleBizInfo class.</p>
 *
 * @author dongnan
 * @since 2024/10/10
 * @version 1.0.0
 */
public class SimpleBizInfo implements Serializable {
    // 序列化版本号
    private static final long serialVersionUID = 1L;

    private String            state;

    private String            name;

    private String            version;

    /**
     * Getter method for property <tt>state</tt>.
     *
     * @return property value of state
     */
    public String getState() {
        return state;
    }

    /**
     * Setter method for property <tt>state</tt>.
     *
     * @param state value to be assigned to property state
     */
    public void setState(BizState state) {
        this.state = state.getBizState();
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>v</tt>.
     *
     * @return property value of v
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>v</tt>.
     *
     * @param version value to be assigned to property v
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public static SimpleBizInfo constructFromBizInfo(BizInfo info) {
        SimpleBizInfo simpleBizInfo = new SimpleBizInfo();
        simpleBizInfo.setState(info.getBizState());
        simpleBizInfo.setName(info.getBizName());
        simpleBizInfo.setVersion(info.getBizVersion());

        return simpleBizInfo;
    }
}
