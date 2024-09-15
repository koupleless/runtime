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
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;

import java.util.List;

import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100003;

/**
 * <p>ArkUtils class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class ArkUtils {
    /**
     * 判断是否是ark模块
     *
     * @return a boolean
     */
    public static boolean isModuleBiz() {
        if (ArkClient.getMasterBiz() == null) {
            return false;
        }
        return ArkClient.getMasterBiz().getBizClassLoader() != Thread.currentThread()
            .getContextClassLoader();
    }

    public static boolean isMasterBiz() {
        if (ArkClient.getMasterBiz() == null) {
            return false;
        }
        return ArkClient.getMasterBiz().getBizClassLoader() == Thread.currentThread()
            .getContextClassLoader();
    }

    /**
     * <p>checkBizExists.</p>
     *
     * @param bizName a {@link java.lang.String} object
     * @since 1.3.1
     */
    public static void checkBizExists(String bizName) {
        List<Biz> bizList = ArkClient.getBizManagerService().getBiz(bizName);
        if (bizList.isEmpty()) {
            throw new BizRuntimeException(E100003, String.format("biz %s does not exist", bizName));
        }
    }
}
