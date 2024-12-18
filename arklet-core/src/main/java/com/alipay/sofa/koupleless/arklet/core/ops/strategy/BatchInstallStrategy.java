/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.arklet.core.ops.strategy;

import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;

import java.util.List;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BatchInstallStrategy.java, v 0.1 2024年12月18日 16:00 立蓬 Exp $
 */
public interface BatchInstallStrategy {
    Map<Integer,List<InstallRequest>> convertToInstallInput(BatchInstallRequest request) throws Throwable;
}