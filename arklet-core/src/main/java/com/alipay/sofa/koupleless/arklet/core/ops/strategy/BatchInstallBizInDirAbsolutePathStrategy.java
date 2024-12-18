/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
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
    private final BatchInstallHelper batchInstallHelper = new BatchInstallHelper();

    private static final String installStrategy = STRATEGY_INSTALL_ONLY_STRATEGY;

    @Override
    public Map<Integer,List<InstallRequest>> convertToInstallInput(BatchInstallRequest request) throws Throwable {
        Map<Integer,List<InstallRequest>> result = new TreeMap<>();

        Map<Integer, List<String>> bizUrls = batchInstallHelper
                .getBizUrlsFromLocalFileSystem(request.getBizDirAbsolutePath());
        for (Map.Entry<Integer, List<String>> entry : bizUrls.entrySet()){
            Integer order = entry.getKey();
            for (String bizUrl : entry.getValue()) {
                result.putIfAbsent(order, new ArrayList<>());
                result.get(order).add(buildInstallRequest(bizUrl));
            }
        }
        return result;
    }

    private InstallRequest buildInstallRequest(String bizAbsolutePath){
        String bizUrl = OSUtils.getLocalFileProtocolPrefix() + bizAbsolutePath;
        Map<String, Object> mainAttributes = batchInstallHelper
                .getMainAttributes(bizAbsolutePath);
        String bizName = (String) mainAttributes.get(Constants.ARK_BIZ_NAME);
        String bizVersion = (String) mainAttributes.get(Constants.ARK_BIZ_VERSION);

        return InstallRequest.builder().bizUrl(bizUrl).bizName(bizName)
                .bizVersion(bizVersion).installStrategy(installStrategy).build();
    }
}