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
package com.alipay.sofa.koupleless.ext.web.gateway;

import lombok.Getter;

/**
 * <p>ForwardItem class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class ForwardItem {

    @Getter
    private final String contextPath;
    @Getter
    private final String host;
    @Getter
    private final String from;

    @Getter
    private final String to;

    /**
     * <p>Constructor for ForwardItem.</p>
     *
     * @param contextPath a {@link java.lang.String} object
     * @param host a {@link java.lang.String} object
     * @param from a {@link java.lang.String} object
     * @param to a {@link java.lang.String} object
     */
    public ForwardItem(String contextPath, String host, String from, String to) {
        this.contextPath = contextPath;
        this.host = host;
        this.from = from;
        this.to = to;
    }
}
