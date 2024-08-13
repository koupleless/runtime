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

import java.util.Date;

/**
 * <p>MqttResponse class.</p>
 *
 * @author dongnan
 * @since 2024/07/10
 * @version 1.0.0
 */
public class MqttResponse {

    /**
     * code
     */
    private long   publishTimestamp;

    /**
     * data
     */
    private Object data;

    /**
     * <p>fromCommandOutput.</p>
     *
     * @param data a {@link java.lang.Object} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse} object
     */
    public static MqttResponse withData(Object data) {
        MqttResponse response = new MqttResponse();
        response.publishTimestamp = new Date().getTime();
        response.data = data;
        return response;
    }

    /**
     * Getter method for property data.
     *
     * @return property value of data
     */
    public Object getData() {
        return data;
    }

    /**
     * Setter method for property data.
     *
     * @param data value to be assigned to property data
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Getter method for property publishTimestamp.
     *
     * @return property value of publishTimestamp
     */
    public long getPublishTimestamp() {
        return publishTimestamp;
    }

    /**
     * Setter method for property publishTimestamp.
     *
     * @param publishTimestamp value to be assigned to property publishTimestamp
     */
    public void setPublishTimestamp(long publishTimestamp) {
        this.publishTimestamp = publishTimestamp;
    }
}
