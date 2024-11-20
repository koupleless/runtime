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
package com.alipay.sofa.koupleless.arklet.core.util;

import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import org.junit.Test;

public class ClassUtilsTest {

    @Test
    public void testGetCustomTunnelClass_Success() {
        ClassUtils.getCustomTunnelClass(
            "com.alipay.sofa.koupleless.arklet.core.tunnel.custom.MockTunnel");
    }

    @Test(expected = ArkletInitException.class)
    public void testGetCustomTunnelClass_NotImplementTunnel() {
        ClassUtils.getCustomTunnelClass(
            "com.alipay.sofa.koupleless.arklet.core.tunnel.custom.MockTunnelNotImplemented");
    }

    @Test(expected = ArkletInitException.class)
    public void testGetCustomTunnelClass_NotExist() {
        ClassUtils.getCustomTunnelClass(
            "com.alipay.sofa.koupleless.arklet.core.tunnel.custom.MockTunnelNotExist");
    }

    @Test
    public void testGetCustomBaseMetadataHookClass_Success() {
        ClassUtils.getBaseMetadataHookImpl(
            "com.alipay.sofa.koupleless.arklet.core.hook.MockBaseMetadataHookImpl");
    }

    @Test(expected = ArkletInitException.class)
    public void testGetCustomBaseMetadataHook_NotImplementTunnel() {
        ClassUtils.getBaseMetadataHookImpl(
            "com.alipay.sofa.koupleless.arklet.core.hook.MockBaseMetadataHookNotImpl");
    }

    @Test(expected = ArkletInitException.class)
    public void testGetCustomBaseMetadataHook_NotExist() {
        ClassUtils.getBaseMetadataHookImpl(
            "com.alipay.sofa.koupleless.arklet.core.hook.MockBaseMetadataHookNotExist");
    }
}
