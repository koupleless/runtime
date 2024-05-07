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
package com.alipay.sofa.koupleless.arklet.core.health.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>BizHealthMeta class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
public class BizHealthMeta {

    private String   bizName;

    private String   bizVersion;

    private BizState bizState;

    private String   webContextPath;

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
     * <p>createBizMeta.</p>
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.health.model.BizHealthMeta} object
     */
    public static BizHealthMeta createBizMeta(Biz biz) {
        AssertUtils.assertNotNull(biz, "can not find biz");
        BizHealthMeta bizHealthMeta = createBizMeta(biz.getBizName(), biz.getBizVersion());
        bizHealthMeta.bizState = biz.getBizState();
        bizHealthMeta.webContextPath = biz.getWebContextPath();
        return bizHealthMeta;
    }

    /**
     * <p>createBizMeta.</p>
     *
     * @param bizName a {@link java.lang.String} object
     * @param bizVersion a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.health.model.BizHealthMeta} object
     */
    public static BizHealthMeta createBizMeta(String bizName, String bizVersion) {
        BizHealthMeta bizHealthMeta = new BizHealthMeta();
        bizHealthMeta.bizName = bizName;
        bizHealthMeta.bizVersion = bizVersion;
        return bizHealthMeta;
    }

    /**
     * <p>createBizMetaList.</p>
     *
     * @param bizList a {@link java.util.List} object
     * @return a {@link java.util.List} object
     */
    public static List<BizHealthMeta> createBizMetaList(List<Biz> bizList) {
        AssertUtils.isTrue(bizList.size() > 0, "no biz found");
        List<BizHealthMeta> bizHealthMetaList = new ArrayList<>();
        for (Biz biz : bizList) {
            bizHealthMetaList.add(createBizMeta(biz));
        }
        return bizHealthMetaList;
    }

}
