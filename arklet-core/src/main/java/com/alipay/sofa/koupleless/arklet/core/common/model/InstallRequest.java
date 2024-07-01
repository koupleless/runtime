/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.arklet.core.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: InstallRequest.java, v 0.1 2024年07月01日 20:02 立蓬 Exp $
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InstallRequest {
    String bizName;
    String bizVersion;
    String bizUrl;
    String[] args;
    Map<String, String> envs;
    boolean useUninstallThenInstallStrategy;
    String bizAlias;
}