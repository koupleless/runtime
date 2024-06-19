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
package com.alipay.sofa.koupleless.plugin.manager.listener;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>StaticBatchInstallEventListener class.</p>
 *
 * @author CodeNoobKingKc2
 * @version $Id: ApplicationContextEventListener, v 0.1 2023-11-21 11:26 CodeNoobKingKc2 Exp $
 */
public class StaticBatchInstallEventListener implements ApplicationListener<ApplicationReadyEvent> {

    // 合并部署是否已经完成，防止重复执行。
    private static final AtomicBoolean isBatchedDeployed = new AtomicBoolean(false);

    /**
     * <p>batchDeployFromLocalDir.</p>
     */
    @SneakyThrows
    public void batchDeployFromLocalDir() {
        String absolutePath = System.getProperty("com.alipay.sofa.ark.static.biz.dir");
        if (StringUtils.isEmpty(absolutePath) || isBatchedDeployed.get()) {
            return;
        }
        ArkletLoggerFactory.getDefaultLogger().info("start to batch deploy from local dir:{}",
            absolutePath);
        UnifiedOperationService operationServiceInstance = ArkletComponentRegistry
            .getOperationServiceInstance();

        BatchInstallResponse batchInstallResponse = operationServiceInstance
            .batchInstall(BatchInstallRequest.builder().bizDirAbsolutePath(absolutePath).build());
        for (Map.Entry<String, ClientResponse> entry : batchInstallResponse.getBizUrlToResponse()
            .entrySet()) {
            ArkletLoggerFactory.getDefaultLogger().info("{}, {}, {}, BatchDeployResult",
                entry.getKey(), entry.getValue().getCode().toString(),
                entry.getValue().getMessage());
        }
        isBatchedDeployed.set(true);
        Preconditions.checkState(batchInstallResponse.getCode() == ResponseCode.SUCCESS,
            "batch deploy failed!");
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 非基座应用直接跳过, 包括普通应用和模块
        if (ArkUtils.isMasterBiz()) {
            // 基座应用启动完成后，执行合并部署。
            batchDeployFromLocalDir();
        }
    }
}
