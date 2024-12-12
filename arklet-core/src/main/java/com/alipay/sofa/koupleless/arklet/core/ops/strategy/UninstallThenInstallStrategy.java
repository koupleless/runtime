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
package com.alipay.sofa.koupleless.arklet.core.ops.strategy;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;

import java.util.List;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: UninstallThenInstallStrategy.java, v 0.1 2024年07月03日 16:57 立蓬 Exp $
 */
public class UninstallThenInstallStrategy implements InstallStrategy {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    @Override
    public ClientResponse install(InstallRequest request) throws Throwable {
        String bizName = request.getBizName();
        String bizVersion = request.getBizVersion();
        String bizUrl = request.getBizUrl();
        String[] args = request.getArgs();
        Map<String, String> envs = request.getEnvs();

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
}
