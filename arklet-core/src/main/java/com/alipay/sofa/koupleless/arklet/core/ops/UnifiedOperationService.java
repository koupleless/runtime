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
package com.alipay.sofa.koupleless.arklet.core.ops;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;

import java.util.List;
import java.util.Map;

/**
 * Unified operation service interface, mainly interacts with the sofa-ark container
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
public interface UnifiedOperationService extends ArkletComponent {

    ClientResponse install(InstallRequest request) throws Throwable;

    /**
     * uninstall biz
     *
     * @param bizName bizName
     * @param bizVersion bizVersion
     * @return response
     * @throws java.lang.Throwable error
     */
    ClientResponse uninstall(String bizName, String bizVersion) throws Throwable;

    /**
     * Batch install multiple biz.
     *
     * @param request 请求。
     * @return response 相应。
     * @throws java.lang.Throwable error
     */
    BatchInstallResponse batchInstall(BatchInstallRequest request) throws Throwable;

    /**
     * query biz list
     *
     * @return biz list
     */
    List<Biz> queryBizList();

    /**
     * switch biz
     *
     * @param bizName bizName
     * @param bizVersion bizVersion
     * @return response
     * @throws java.lang.Throwable error
     */
    ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable;

}
