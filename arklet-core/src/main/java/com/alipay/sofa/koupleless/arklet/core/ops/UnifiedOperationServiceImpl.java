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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.koupleless.arklet.core.command.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.BatchInstallBizInDirAbsolutePathStrategy;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.BatchInstallBizInRequestStrategy;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.BatchInstallStrategy;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest.InstallStrategyEnum;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.InstallStrategy;
import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>UnifiedOperationServiceImpl class.</p>
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
@Singleton
public class UnifiedOperationServiceImpl implements UnifiedOperationService {
    private final BatchInstallStrategy batchInstallBizInDirAbsolutePathStrategy = new BatchInstallBizInDirAbsolutePathStrategy();

    private final BatchInstallStrategy batchInstallBizInRequestStrategy         = new BatchInstallBizInRequestStrategy();

    /** {@inheritDoc} */
    @Override
    public void init() {

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {

    }

    @Override
    public ClientResponse install(InstallRequest request) throws Throwable {
        InstallStrategy installStrategy = InstallStrategyEnum
            .getStrategyByName(request.getInstallStrategy());
        return installStrategy.install(request);
    }

    /**
     * <p>safeBatchInstall.</p>
     *
     * @param bizRequest a {@link InstallRequest} object
     * @return a {@link com.alipay.sofa.ark.api.ClientResponse} object
     */
    public ClientResponse safeBatchInstall(InstallRequest bizRequest) {
        try {
            return install(bizRequest);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new ClientResponse().setCode(ResponseCode.FAILED)
                .setMessage(String.format("internal exception: %s", throwable.getMessage()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public ClientResponse uninstall(String bizName, String bizVersion) throws Throwable {
        return ArkClient.uninstallBiz(bizName, bizVersion);
    }

    /** {@inheritDoc} */
    @Override
    public BatchInstallResponse batchInstall(BatchInstallRequest request) throws Throwable {
        long startTimestamp = System.currentTimeMillis();

        BatchInstallStrategy batchInstallStrategy = getBatchInstallStrategy(request);
        Map<Integer, List<InstallRequest>> installRequestsWithOrder = batchInstallStrategy
            .convertToInstallInput(request);

        ThreadPoolExecutor executorService = ExecutorServiceManager.getArkBizOpsExecutor();
        Map<String, ClientResponse> bizUrlToInstallResult = new HashMap<>();
        boolean hasFailed = false;
        for (Entry<Integer, List<InstallRequest>> entry : installRequestsWithOrder.entrySet()) {
            List<InstallRequest> bizRequestInSameOrder = entry.getValue();
            List<CompletableFuture<ClientResponse>> futures = new ArrayList<>();
            for (InstallRequest bizRequest : bizRequestInSameOrder) {
                futures.add(CompletableFuture.supplyAsync(() -> safeBatchInstall(bizRequest),
                    executorService));
            }

            // wait for all install futures done
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            int counter = 0;
            for (CompletableFuture<ClientResponse> future : futures) {
                ClientResponse clientResponse = future.get();
                InstallRequest bizRequest = bizRequestInSameOrder.get(counter);
                bizUrlToInstallResult.put(bizRequest.getBizUrl(), clientResponse);
                hasFailed = hasFailed || clientResponse.getCode() != ResponseCode.SUCCESS;
                counter++;
            }
        }

        long endTimestamp = System.currentTimeMillis();
        ArkletLoggerFactory.getDefaultLogger().info("batch install cost {} ms",
            endTimestamp - startTimestamp);

        return BatchInstallResponse.builder()
            .code(hasFailed ? ResponseCode.FAILED : ResponseCode.SUCCESS)
            .message(hasFailed ? "batch install failed" : "batch install success")
            .bizUrlToResponse(bizUrlToInstallResult).build();
    }

    private BatchInstallStrategy getBatchInstallStrategy(BatchInstallRequest request) {
        if (StringUtil.isNotEmpty(request.getBizDirAbsolutePath())) {
            return batchInstallBizInRequestStrategy;
        }
        return batchInstallBizInRequestStrategy;
    }

    /** {@inheritDoc} */
    @Override
    public List<Biz> queryBizList() {
        return ArkClient.getBizManagerService().getBizInOrder();
    }

    /** {@inheritDoc} */
    @Override
    public ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable {
        return ArkClient.switchBiz(bizName, bizVersion);
    }
}
