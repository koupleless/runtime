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
package com.alipay.sofa.koupleless.arklet.core.hook;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import org.junit.Assert;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongnan
 * @since 2024/09/04
 */
public class BaseMetadataHookTest extends BaseTest {
    @Test
    public void getRuntimeEnv() {
        String env = baseMetadataHook.getRuntimeEnv();
        Assert.assertEquals("default", env);
    }

    @Test
    public void getName() {
        baseMetadataHook.getIdentity();
    }

    @Test
    public void getNetworkInfo() {
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
        Assert.assertEquals(networkInfoMap.get(baseMetadataHook.getLocalIP()),
            baseMetadataHook.getLocalHostName());
    }
}
