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
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.koupleless.arklet.core.command.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final ArkletLogger LOGGER             = ArkletLoggerFactory.getDefaultLogger();

    private BatchInstallHelper        batchInstallHelper = new BatchInstallHelper();

    /** {@inheritDoc} */
    @Override
    public void init() {

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {

    }

    /** {@inheritDoc} */
    @Override
    public ClientResponse install(String bizName, String bizVersion, String bizUrl, String[] args,
                                  Map<String, String> envs,
                                  boolean useUninstallThenInstallStrategy) throws Throwable {
        if (useUninstallThenInstallStrategy) {
            return doUninstallThenInstallStrategy(bizName, bizVersion, bizUrl, args, envs);
        }
        return doInstallThenUninstallStrategy(bizName, bizVersion, bizUrl, args, envs);
    }

    private ClientResponse doUninstallThenInstallStrategy(String bizName, String bizVersion,
                                                          String bizUrl, String[] args,
                                                          Map<String, String> envs) throws Throwable {
        // uninstall first
        List<Biz> bizListToUninstall = ArkClient.getBizManagerService().getBiz(bizName);
        LOGGER.info("start to uninstall bizLists: {}", bizListToUninstall);
        for (Biz biz : bizListToUninstall) {
            ArkClient.uninstallBiz(biz.getBizName(), biz.getBizVersion());
        }

        LOGGER.info("success uninstall bizLists: {}", bizListToUninstall);
        LOGGER.info("start to install biz: {},{},{}", bizName, bizVersion, bizUrl);
        // install
        BizOperation bizOperation = new BizOperation()
            .setOperationType(BizOperation.OperationType.INSTALL);
        bizOperation.setBizName(bizName);
        bizOperation.setBizVersion(bizVersion);
        bizOperation.putParameter(Constants.CONFIG_BIZ_URL, bizUrl);
        return ArkClient.installOperation(bizOperation, args, envs);
    }

    private ClientResponse doInstallThenUninstallStrategy(String bizName, String bizVersion,
                                                          String bizUrl, String[] args,
                                                          Map<String, String> envs) throws Throwable {
        throw new UnsupportedOperationException(
            "have not implemented uninstallAfterInstallStrategy");
    }

    /**
     * <p>safeBatchInstall.</p>
     *
     * @param bizAbsolutePath a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.ark.api.ClientResponse} object
     */
    public ClientResponse safeBatchInstall(String bizAbsolutePath,
                                           boolean useUninstallThenInstallStrategy) {
        try {
            String bizUrl = OSUtils.getLocalFileProtocolPrefix() + bizAbsolutePath;
            Map<String, Object> mainAttributes = batchInstallHelper
                .getMainAttributes(bizAbsolutePath);
            String bizName = (String) mainAttributes.get(Constants.ARK_BIZ_NAME);
            String bizVersion = (String) mainAttributes.get(Constants.ARK_BIZ_VERSION);
            return install(bizName, bizVersion, bizUrl, null, null,
                useUninstallThenInstallStrategy);
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
        Map<Integer, List<String>> bizUrls = batchInstallHelper
            .getBizUrlsFromLocalFileSystem(request.getBizDirAbsolutePath());
        ArkletLoggerFactory.getDefaultLogger().info("Found biz jar files: {}", bizUrls);
        ThreadPoolExecutor executorService = ExecutorServiceManager.getArkBizOpsExecutor();

        Map<String, ClientResponse> bizUrlToInstallResult = new HashMap<>();
        boolean hasFailed = false;
        for (Map.Entry<Integer, List<String>> entry : bizUrls.entrySet()) {
            List<String> bizUrlsInSameOrder = entry.getValue();
            List<CompletableFuture<ClientResponse>> futures = new ArrayList<>();
            for (String bizUrl : bizUrlsInSameOrder) {
                futures.add(CompletableFuture.supplyAsync(
                    () -> safeBatchInstall(bizUrl, request.isUseUninstallThenInstallStrategy()),
                    executorService));
            }

            // wait for all install futures done
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            int counter = 0;
            for (CompletableFuture<ClientResponse> future : futures) {
                ClientResponse clientResponse = future.get();
                String bizUrl = bizUrlsInSameOrder.get(counter);
                bizUrlToInstallResult.put(bizUrl, clientResponse);
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
