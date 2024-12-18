/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
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

    private final BatchInstallHelper batchInstallHelper = new BatchInstallHelper();
    @Override
    public Map<Integer, List<InstallRequest>> convertToInstallInput(BatchInstallRequest request) throws Throwable {
        Map<Integer,List<InstallRequest>> result = new TreeMap<>();
        for (InstallRequest installRequest : request.getInstallRequests()){
            Integer order = parsePriority(installRequest);
            result.putIfAbsent(order, new ArrayList<>());
            result.get(order).add(installRequest);
        }
        return result;
    }

    @SneakyThrows
    private Integer parsePriority(InstallRequest installRequest){
        URL url = new URL(installRequest.getBizUrl());
        File bizFile = ArkClient.createBizSaveFile(installRequest.getBizName(), installRequest.getBizVersion());
        FileUtils.copyInputStreamToFile(url.openStream(), bizFile);

        Map<String, Object> mainAttributes = batchInstallHelper
                .getMainAttributes(bizFile.getAbsolutePath());
        org.apache.commons.io.FileUtils.deleteQuietly(bizFile);
        return Integer.valueOf(
                mainAttributes.getOrDefault("priority", PriorityOrdered.DEFAULT_PRECEDENCE)
                        .toString());
    }
}