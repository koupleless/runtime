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
package com.alipay.sofa.koupleless.arklet.core.command.meta;

import com.alipay.sofa.koupleless.arklet.core.api.model.ResponseCode;

/**
 * <p>Output class.</p>
 *
 * @author mingmen
 * @since 2023/6/8
 * @version 1.0.0
 */
public class Output<T> {

    private ResponseCode code;
    private String       message;
    private T            data;

    private Output() {
    }

    /**
     * <p>ofSuccess.</p>
     *
     * @param data a T object
     * @param <T> a T class
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Output} object
     */
    public static <T> Output<T> ofSuccess(T data) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.SUCCESS;
        output.data = data;
        return output;
    }

    /**
     * <p>ofFailed.</p>
     *
     * @param message a {@link java.lang.String} object
     * @param <T> a T class
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Output} object
     */
    public static <T> Output<T> ofFailed(String message) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.FAILED;
        output.message = message;
        return output;
    }

    /**
     * <p>success.</p>
     *
     * @return a boolean
     */
    public boolean success() {
        return ResponseCode.SUCCESS.equals(code);
    }

    /**
     * <p>failed.</p>
     *
     * @return a boolean
     */
    public boolean failed() {
        return ResponseCode.FAILED.equals(code);
    }

    /**
     * <p>ofFailed.</p>
     *
     * @param data a T object
     * @param message a {@link java.lang.String} object
     * @param <T> a T class
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.command.meta.Output} object
     */
    public static <T> Output<T> ofFailed(T data, String message) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.FAILED;
        output.data = data;
        output.message = message;
        return output;
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
     * <p>Getter for the field <code>data</code>.</p>
     *
     * @return a T object
     */
    public T getData() {
        return data;
    }

    /**
     * <p>Setter for the field <code>data</code>.</p>
     *
     * @param data a T object
     */
    public void setData(T data) {
        this.data = data;
    }
}
