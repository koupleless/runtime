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
package com.alipay.sofa.koupleless.arklet.core.health.model;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.BYTE_TO_MB;
import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.MB;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ClientMetrics.java, v 0.1 2024年07月02日 13:32 立蓬 Exp $
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientMetrics {

    private String              timestamp;

    private ClientMemoryMetrics clientMetaspaceMetrics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientMemoryMetrics {
        private long init;
        private long used;
        private long committed;
        private long max;
    }
}