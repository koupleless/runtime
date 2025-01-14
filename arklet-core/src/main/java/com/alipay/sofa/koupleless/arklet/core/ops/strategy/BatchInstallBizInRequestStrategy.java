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
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import com.alipay.sofa.koupleless.arklet.core.ops.BatchInstallHelper;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BatchInstallBizInRequestStrategy.java, v 0.1 2024年12月18日 16:54 立蓬 Exp $
 */
public class BatchInstallBizInRequestStrategy implements BatchInstallStrategy {

    @Override
    public Map<Integer, List<InstallRequest>> convertToInstallInput(BatchInstallRequest request) throws Throwable {
        Map<Integer, List<InstallRequest>> result = new TreeMap<>();
        for (InstallRequest installRequest : request.getInstallRequests()) {
            Integer order = parsePriority(installRequest);
            result.putIfAbsent(order, new ArrayList<>());
            result.get(order).add(installRequest);
        }
        return result;
    }

    @SneakyThrows
    private Integer parsePriority(InstallRequest installRequest) {
        URL url = new URL(installRequest.getBizUrl());
        File bizFile = ArkClient.createBizSaveFile(installRequest.getBizName(),
            installRequest.getBizVersion());
        FileUtils.copyInputStreamToFile(url.openStream(), bizFile);

        Map<String, Object> mainAttributes = BatchInstallHelper
            .getMainAttributes(bizFile.getAbsolutePath());
        org.apache.commons.io.FileUtils.deleteQuietly(bizFile);
        return Integer.valueOf(
            mainAttributes.getOrDefault("priority", PriorityOrdered.DEFAULT_PRECEDENCE).toString());
    }
}