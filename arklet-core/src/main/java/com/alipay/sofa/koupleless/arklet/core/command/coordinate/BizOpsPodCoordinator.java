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
package com.alipay.sofa.koupleless.arklet.core.command.coordinate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.ark.common.util.StringUtils;

/**
 * <p>
 * BizOpsPodCoordinator class.
 * </p>
 *
 * @author liuzhuoheng
 * @since 2025/7/15
 * @version 1.0.0
 */
public class BizOpsPodCoordinator {

    /**
     * bizIdentityLockMap
     * key: bizIdentity, value: bizModelVersion
     */
    private static final Map<String, String> bizIdentityLockMap = new ConcurrentHashMap<>();

    /**
     * <p>
     * save.
     * </p>
     * 
     * @param bizIdentity     a {@link java.lang.String} object
     * @param bizModelVersion a {@link java.lang.String} object
     * @return
     */
    public static void save(String bizIdentity, String bizModelVersion) {
        if (StringUtils.isEmpty(bizIdentity)) {
            return;
        }
        if (StringUtils.isEmpty(bizModelVersion)) {
            bizModelVersion = StringUtils.EMPTY_STRING;
        }
        bizIdentityLockMap.put(bizIdentity, bizModelVersion);
    }

    /**
     * <p>
     * remove.
     * </p>
     *
     * @param bizIdentity     a {@link java.lang.String} object
     * @param bizModelVersion a {@link java.lang.String} object
     * @return
     */
    public static void remove(String bizIdentity, String bizModelVersion) {
        if (StringUtils.isEmpty(bizIdentity)) {
            return;
        }
        if (StringUtils.isEmpty(bizModelVersion)) {
            bizIdentityLockMap.remove(bizIdentity);
            return;
        }
        bizIdentityLockMap.remove(bizIdentity, bizModelVersion);
    }

    /**
     * <p>
     * canAccess.
     * </p>
     * 判断是否可以访问指定的业务模块，基于业务模块版本的协调机制
     * 
     * @param bizIdentity     业务模块标识 (bizName:bizVersion)
     * @param bizModelVersion 业务模块模型版本，用于命令协调和防止过期命令执行
     * @return 是否允许访问该业务模块
     */
    public static boolean canAccess(String bizIdentity, String bizModelVersion) {
        // 判断逻辑说明：
        // Case 1: bizModelVersion 为空 - 兼容性处理，允许访问（兼容旧版本 module-controller，arktcl,
        // pod-not-exist 和 pod 紧急删除场景）
        // Case 2: bizIdentityLockMap 中没有该 bizIdentity 的记录，允许访问（安装时不带
        // BizModelVersion，卸载时带上 BizModelVersion）
        // Case 3: bizIdentityLockMap 中的版本与当前请求的版本匹配 - 版本一致，确认卸载的是该 Biz，允许访问
        // 只有当 bizModelVersion 不为空且存在 bizModelVersion 且不匹配时，才拒绝访问（防止旧的卸载命令执行）
        return StringUtils.isEmpty(bizModelVersion)
               || StringUtils.isEmpty(bizIdentityLockMap.get(bizIdentity))
               || bizIdentityLockMap.get(bizIdentity).equals(bizModelVersion);
    }
}