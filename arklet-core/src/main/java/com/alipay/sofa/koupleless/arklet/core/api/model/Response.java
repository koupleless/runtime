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
package com.alipay.sofa.koupleless.arklet.core.api.model;

import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;

import java.util.UUID;

/**
 * <p>Response class.</p>
 *
 * @author mingmen
 * @since 2023/6/26
 * @version 1.0.0
 */
public class Response {

    /**
     * code
     */
    private ResponseCode code;

    /**
     * message
     */
    private String       message;

    /**
     * data
     */
    private Object       data;

    /**
     * error stack trace
     */
    private String       errorStackTrace;

    /**
     *  baseID
     */
    private String       baseID;

    /**
     * <p>fromCommandOutput.</p>
     *
     * @param output a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Output} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.Response} object
     */
    public static Response fromCommandOutput(Output output, String baseID) {
        Response response = new Response();
        response.code = output.getCode();
        response.data = output.getData();
        response.message = output.getMessage();
        response.baseID = baseID;
        return response;
    }

    /**
     * <p>success.</p>
     *
     * @param data a {@link java.lang.Object} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.Response} object
     */
    public static Response success(Object data) {
        Response response = new Response();
        response.code = ResponseCode.SUCCESS;
        response.data = data;
        return response;
    }

    /**
     * <p>failed.</p>
     *
     * @param message a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.Response} object
     */
    public static Response failed(String message) {
        Response response = new Response();
        response.code = ResponseCode.FAILED;
        response.message = message;
        return response;
    }

    /**
     * <p>notFound.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.Response} object
     */
    public static Response notFound() {
        Response response = new Response();
        response.code = ResponseCode.CMD_NOT_FOUND;
        response.message = "please follow the doc";
        return response;
    }

    /**
     * <p>internalError.</p>
     *
     * @param message a {@link java.lang.String} object
     * @param errorStackTrace a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.Response} object
     */
    public static Response internalError(String message, String errorStackTrace) {
        Response response = new Response();
        response.code = ResponseCode.CMD_PROCESS_INTERNAL_ERROR;
        response.message = message;
        response.errorStackTrace = errorStackTrace;
        return response;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getMessage() {
        return message;
    }

    /**
     * <p>Setter for the field <code>message</code>.</p>
     *
     * @param message a {@link java.lang.String} object
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * <p>Getter for the field <code>data</code>.</p>
     *
     * @return a {@link java.lang.Object} object
     */
    public Object getData() {
        return data;
    }

    /**
     * <p>Setter for the field <code>data</code>.</p>
     *
     * @param data a {@link java.lang.Object} object
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * <p>Getter for the field <code>code</code>.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.api.model.ResponseCode} object
     */
    public ResponseCode getCode() {
        return code;
    }

    /**
     * <p>Setter for the field <code>code</code>.</p>
     *
     * @param code a {@link com.alipay.sofa.koupleless.arklet.core.api.model.ResponseCode} object
     */
    public void setCode(ResponseCode code) {
        this.code = code;
    }

    /**
     * <p>Getter for the field <code>errorStackTrace</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    /**
     * <p>Setter for the field <code>errorStackTrace</code>.</p>
     *
     * @param errorStackTrace a {@link java.lang.String} object
     */
    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    /**
     * Getter method for property <tt>baseID</tt>.
     *
     * @return property value of baseID
     */
    public String getBaseID() {
        return baseID;
    }

    /**
     * Setter method for property <tt>baseID</tt>.
     *
     * @param baseID value to be assigned to property baseID
     */
    public void setBaseID(String baseID) {
        this.baseID = baseID;
    }
}
