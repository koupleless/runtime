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

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import com.alipay.sofa.koupleless.arklet.core.ops.BatchInstallHelper;
import com.alipay.sofa.koupleless.common.util.OSUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_INSTALL_ONLY_STRATEGY;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BatchInstallBizInDirStrategy.java, v 0.1 2024年12月18日 16:00 立蓬 Exp $
 */
public class BatchInstallBizInDirAbsolutePathStrategy implements BatchInstallStrategy {
    private static final String installStrategy = STRATEGY_INSTALL_ONLY_STRATEGY;

    @Override
    public Map<Integer, List<InstallRequest>> convertToInstallInput(BatchInstallRequest request) throws Throwable {
        Map<Integer, List<InstallRequest>> result = new TreeMap<>();

        Map<Integer, List<String>> bizUrls = BatchInstallHelper
            .getBizUrlsFromLocalFileSystem(request.getBizDirAbsolutePath());
        for (Map.Entry<Integer, List<String>> entry : bizUrls.entrySet()) {
            Integer order = entry.getKey();
            for (String bizUrl : entry.getValue()) {
                result.putIfAbsent(order, new ArrayList<>());
                result.get(order).add(buildInstallRequest(bizUrl));
            }
        }
        return result;
    }

    private InstallRequest buildInstallRequest(String bizAbsolutePath) {
        String bizUrl = OSUtils.getLocalFileProtocolPrefix() + bizAbsolutePath;
        Map<String, Object> mainAttributes = BatchInstallHelper.getMainAttributes(bizAbsolutePath);
        String bizName = (String) mainAttributes.get(Constants.ARK_BIZ_NAME);
        String bizVersion = (String) mainAttributes.get(Constants.ARK_BIZ_VERSION);

        return InstallRequest.builder().bizUrl(bizUrl).bizName(bizName).bizVersion(bizVersion)
            .installStrategy(installStrategy).build();
    }
}