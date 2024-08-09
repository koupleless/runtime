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
package com.alipay.sofa.koupleless.arklet.springboot.starter.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alipay.sofa.koupleless.arklet.springboot.starter.properties.ArkletProperties.PREFIX;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArkletProperties.java, v 0.1 2024年07月02日 13:48 立蓬 Exp $
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(PREFIX)
public class ArkletProperties {
    public static final String  PREFIX    = "com.alipay.sofa.koupleless.runtime";

    private MonitorProperties   monitor   = new MonitorProperties();

    private OperationProperties operation = new OperationProperties();

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonitorProperties {

        private int     metaspaceThreshold = 85;

        private boolean metaspaceCheck     = false;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OperationProperties {

        private int silenceSecondsBeforeUninstall = 0;
    }
}