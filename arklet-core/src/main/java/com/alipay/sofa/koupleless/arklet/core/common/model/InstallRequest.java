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

import com.alipay.sofa.koupleless.arklet.core.ops.strategy.InstallOnlyStrategy;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.InstallStrategy;
import com.alipay.sofa.koupleless.arklet.core.ops.strategy.UninstallThenInstallStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_INSTALL_ONLY_STRATEGY;
import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_UNINSTALL_THEN_INSTALL;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: InstallRequest.java, v 0.1 2024年07月01日 20:02 立蓬 Exp $
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InstallRequest {
    String              bizName;
    String              bizVersion;
    String              bizUrl;
    String[]            args;
    Map<String, String> envs;
    String              installStrategy;

    public enum InstallStrategyEnum {
                                     UNINSTALL_THEN_INSTALL(STRATEGY_UNINSTALL_THEN_INSTALL,
                                                            new UninstallThenInstallStrategy()), INSTALL_ONLY(
                STRATEGY_INSTALL_ONLY_STRATEGY,
                                                                                                              new InstallOnlyStrategy());

        private String          name;

        @Getter
        private InstallStrategy installStrategy;

        InstallStrategyEnum(String name, InstallStrategy installStrategy) {
            this.name = name;
            this.installStrategy = installStrategy;
        }

        public static InstallStrategy getStrategyByName(String name) {
            for (InstallStrategyEnum installStrategyEnum : InstallStrategyEnum.values()) {
                if (installStrategyEnum.name.equals(name)) {
                    return installStrategyEnum.installStrategy;
                }
            }
            return INSTALL_ONLY.installStrategy;
        }
    }
}
