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
package com.alipay.sofa.koupleless.arklet.core.command.builtin.model;

import com.alipay.sofa.ark.spi.model.BizInfo.BizStateRecord;
import com.alipay.sofa.ark.spi.model.BizState;

import java.util.List;

/**
 * <p>BizInfo class.</p>
 *
 * @author mingmen
 * @date 2023/6/14
 * @version 1.0.0
 */
public class BizInfo {
    private String               bizName;

    private String               bizVersion;

    private BizState             bizState;

    private String               mainClass;

    private String               webContextPath;

    private ClassLoader          classLoader;

    private List<BizStateRecord> bizStateRecords;

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
     * <p>Getter for the field <code>bizState</code>.</p>
     *
     * @return a {@link com.alipay.sofa.ark.spi.model.BizState} object
     */
    public BizState getBizState() {
        return bizState;
    }

    /**
     * <p>Setter for the field <code>bizState</code>.</p>
     *
     * @param bizState a {@link com.alipay.sofa.ark.spi.model.BizState} object
     */
    public void setBizState(BizState bizState) {
        this.bizState = bizState;
    }

    /**
     * <p>Getter for the field <code>mainClass</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * <p>Setter for the field <code>mainClass</code>.</p>
     *
     * @param mainClass a {@link java.lang.String} object
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * <p>Getter for the field <code>webContextPath</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getWebContextPath() {
        return webContextPath;
    }

    /**
     * <p>Setter for the field <code>webContextPath</code>.</p>
     *
     * @param webContextPath a {@link java.lang.String} object
     */
    public void setWebContextPath(String webContextPath) {
        this.webContextPath = webContextPath;
    }

    /**
     * <p>Getter for the field <code>classLoader</code>.</p>
     *
     * @return a {@link java.lang.ClassLoader} object
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * <p>Setter for the field <code>classLoader</code>.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * <p>Getter for the field <code>bizStateRecords</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<BizStateRecord> getBizStateRecords() {
        return bizStateRecords;
    }

    /**
     * <p>Setter for the field <code>bizStateRecords</code>.</p>
     *
     * @param bizStateRecords a {@link java.util.List} object
     */
    public void setBizStateRecords(List<BizStateRecord> bizStateRecords) {
        this.bizStateRecords = bizStateRecords;
    }
}
