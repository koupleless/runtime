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
package com.alipay.sofa.koupleless.arklet.core.hook.network;

import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseNetworkInfo;

import java.net.*;
import java.util.Enumeration;

/**
 * <p>NetworkInfoHookImpl.</p>
 *
 * @author dongnan
 * @since 2024/10/24
 * @version 1.0.0
 */
public class BaseNetworkInfoHookImpl implements BaseNetworkInfoHook {

    private InetAddress localHost = null;

    {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        // find first non-loopback ipv4 address
                        localHost = address;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            throw new ArkletInitException("getLocalNetworkInfo error", e);
        }
    }

    @Override
    public BaseNetworkInfo getNetworkInfo() {
        String localIP = "unknown";
        String localHostName = "unknown";
        if (localHost != null) {
            localIP = localHost.getHostAddress();
            localHostName = localHost.getHostName();
        }
        return BaseNetworkInfo.builder().localIP(localIP).localHostName(localHostName).build();
    }

}
