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
package com.alipay.sofa.koupleless.arklet.core.command.meta.bizops;

import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;

/**
 * <p>ArkBizMeta class.</p>
 *
 * @author mingmen
 * @since 2023/8/21
 * @version 1.0.0
 */
public class ArkBizMeta extends InputMeta {
    private String  bizName;
    private String  bizVersion;
    private String  requestId;
    private String  bizModelVersion;
    private boolean async;

    /**
     * <p>Getter for the field <code>bizName</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getBizName() {
        return bizName;
    }

    /**
     * <p>Setter for the field <code>bizName</code>.</p>
     *
     * @param bizName a {@link java.lang.String} object
     */
    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    /**
     * <p>Getter for the field <code>bizVersion</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getBizVersion() {
        return bizVersion;
    }

    /**
     * <p>Setter for the field <code>bizVersion</code>.</p>
     *
     * @param bizVersion a {@link java.lang.String} object
     */
    public void setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
    }

    /**
     * <p>Getter for the field <code>requestId</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * <p>Setter for the field <code>requestId</code>.</p>
     *
     * @param requestId a {@link java.lang.String} object
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * <p>Getter for the field <code>bizModelVersion</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getBizModelVersion() {
        return bizModelVersion;
    }

    /**
     * <p>Setter for the field <code>bizModelVersion</code>.</p>
     *
     * @param bizModelVersion a {@link java.lang.String} object
     */
    public void setBizModelVersion(String bizModelVersion) {
        this.bizModelVersion = bizModelVersion;
    }

    /**
     * <p>isAsync.</p>
     *
     * @return a boolean
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * <p>Setter for the field <code>async</code>.</p>
     *
     * @param async a boolean
     */
    public void setAsync(boolean async) {
        this.async = async;
    }
}
