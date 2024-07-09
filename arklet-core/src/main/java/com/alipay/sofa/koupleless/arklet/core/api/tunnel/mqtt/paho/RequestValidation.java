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
package com.alipay.sofa.koupleless.arklet.core.api.tunnel.mqtt.paho;

import java.util.Map;

/**
 * <p>RequestValidation class.</p>
 *
 * @author dongnan
 * @since 2024/7/5
 * @version 1.0.0
 */
public class RequestValidation {
    private boolean             pass;
    private String              message;
    private boolean             cmdSupported;
    private String              cmd;
    private Map<String, Object> cmdContent;

    /**
     * <p>Constructor for RequestValidation.</p>
     */
    public RequestValidation() {
    }

    /**
     * <p>notPass.</p>
     *
     * @param message a {@link String} object
     * @return a {@link RequestValidation} object
     */
    public static RequestValidation notPass(String message) {
        RequestValidation validation = new RequestValidation();
        validation.pass = false;
        validation.message = message;
        return validation;
    }

    /**
     * <p>passed.</p>
     *
     * @param cmdSupported a boolean
     * @param cmd a {@link String} object
     * @param cmdContent a {@link Map} object
     * @return a {@link RequestValidation} object
     */
    public static RequestValidation passed(boolean cmdSupported, String cmd,
                                           Map<String, Object> cmdContent) {
        RequestValidation validation = new RequestValidation();
        validation.pass = true;
        validation.cmdSupported = cmdSupported;
        validation.cmd = cmd;
        validation.cmdContent = cmdContent;
        return validation;
    }

    /**
     * <p>isPass.</p>
     *
     * @return a boolean
     */
    public boolean isPass() {
        return pass;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link String} object
     */
    public String getMessage() {
        return message;
    }

    /**
     * <p>isCmdSupported.</p>
     *
     * @return a boolean
     */
    public boolean isCmdSupported() {
        return cmdSupported;
    }

    /**
     * <p>Getter for the field <code>cmd</code>.</p>
     *
     * @return a {@link String} object
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * <p>Getter for the field <code>cmdContent</code>.</p>
     *
     * @return a {@link Map} object
     */
    public Map<String, Object> getCmdContent() {
        return cmdContent;
    }
}
