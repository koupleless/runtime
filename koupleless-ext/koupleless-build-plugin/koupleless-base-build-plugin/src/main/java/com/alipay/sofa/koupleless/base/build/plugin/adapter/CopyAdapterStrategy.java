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
package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import java.io.File;

/**
 * <p>CopyAdapterStrategy interface.</p>
 *
 * @author CodeNoobKing
 * @since 2024/3/18
 * @version 1.0.0
 */
public interface CopyAdapterStrategy {
    /**
     * <p>copy.</p>
     *
     * @param buildDir a {@link java.io.File} object
     * @param entryName a {@link java.lang.String} object
     * @param content an array of byte objects
     * @throws java.lang.Throwable if any.
     */
    void copy(File buildDir, String entryName, byte[] content) throws Throwable;
}
