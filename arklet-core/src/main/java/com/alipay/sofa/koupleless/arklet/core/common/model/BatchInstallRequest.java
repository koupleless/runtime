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
package com.alipay.sofa.koupleless.arklet.core.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_INSTALL_ONLY_STRATEGY;

/**
 * 合并部署请求。
 *
 * @author CodeNoobKingKc2
 * @version $Id: BatchInstallRequest, v 0.1 2023-11-20 15:21 CodeNoobKingKc2 Exp $
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BatchInstallRequest {
    /**
     * 本地文件系统目录。
     */
    private String           bizDirAbsolutePath;
    /**
     * 静态合并部署，默认没有老版本模块，可以直接使用普通安装策略。
     */
    @Builder.Default
    private String           installStrategy = STRATEGY_INSTALL_ONLY_STRATEGY;

    /**
     * 模块批量发布请求。
     */
    private InstallRequest[] installRequests = new InstallRequest[0];
}
