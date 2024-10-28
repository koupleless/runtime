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
package com.alipay.sofa.koupleless.arklet.core.hooks.network;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseNetworkInfo;
import org.junit.Assert;
import org.junit.Test;

import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongnan
 * @since 2024/10/24
 */
public class BaseNetworkInfoHookTest extends BaseTest {
    @Test
    public void getNetworkInfo() {
        BaseNetworkInfo networkInfo = networkInfoHook.getNetworkInfo();
        Map<String, String> networkInfoMap = new HashMap<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        networkInfoMap.put(address.getHostAddress(), address.getHostName());
                    }
                }
            }
        } catch (SocketException e) {
            throw new ArkletInitException("getLocalNetworkInfo error", e);
        }
        Assert.assertEquals(networkInfoMap.get(networkInfo.getLocalIP()),
            networkInfo.getLocalHostName());
    }
}
