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
package com.alipay.sofa.koupleless.arklet.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.HttpTunnel;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHookImpl;
import com.alipay.sofa.koupleless.arklet.core.hook.network.BaseNetworkInfoHook;
import com.alipay.sofa.koupleless.arklet.core.hook.network.BaseNetworkInfoHookImpl;
import com.alipay.sofa.koupleless.arklet.core.util.ClassUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * <p>ApiClient class.</p>
 *
 * @author mingmen
 * @since 2023/6/8
 * @version 1.0.0
 */

@Singleton
public class ApiClient implements ArkletComponent {

    private static final UUID          baseID                          = UUID.randomUUID();

    private static final List<Tunnel>  tunnelList                      = new ArrayList<>(8);

    private final static String        CUSTOM_TUNNEL_CLASS             = "koupleless.arklet.custom.tunnel.classname";

    private final static String        CUSTOM_BASE_METADATA_HOOK_CLASS = "koupleless.arklet.custom.base.metadata.classname";

    private final static String        CUSTOM_NETWORK_INFO_HOOK_CLASS  = "koupleless.arklet.custom.network.info.classname";

    @Inject
    private CommandService             commandService;

    private static BaseMetadataHook    baseMetadataHook;

    private static BaseNetworkInfoHook baseNetworkInfoHook;

    static {
        Injector injector = Guice.createInjector(new TunnelGuiceModule());
        for (Binding<Tunnel> binding : injector.findBindingsByType(new TypeLiteral<Tunnel>() {
        })) {
            tunnelList.add(binding.getProvider().get());
        }

        baseMetadataHook = new BaseMetadataHookImpl();

        String customBaseMetadataHookClassName = EnvironmentUtils
            .getProperty(CUSTOM_BASE_METADATA_HOOK_CLASS);

        if (customBaseMetadataHookClassName != null && !customBaseMetadataHookClassName.isEmpty()) {
            baseMetadataHook = ClassUtils.getBaseMetadataHookImpl(customBaseMetadataHookClassName);
        }

        baseNetworkInfoHook = new BaseNetworkInfoHookImpl();

        String customNetworkInfoHookClassName = EnvironmentUtils
            .getProperty(CUSTOM_NETWORK_INFO_HOOK_CLASS);

        if (customNetworkInfoHookClassName != null && !customNetworkInfoHookClassName.isEmpty()) {
            baseNetworkInfoHook = ClassUtils.getNetworkInfoHook(customNetworkInfoHookClassName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.init(commandService, baseMetadataHook, baseNetworkInfoHook, baseID);
            tunnel.run();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.shutdown();
        }
    }

    /**
     * <p>getTunnels.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<Tunnel> getTunnels() {
        return tunnelList;
    }

    public BaseMetadataHook getMetadataHook() {
        return baseMetadataHook;
    }

    public BaseNetworkInfoHook getNetworkInfoHook() {
        return baseNetworkInfoHook;
    }

    private static class TunnelGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<Tunnel> tunnelMultibinder = Multibinder.newSetBinder(binder(),
                Tunnel.class);
            tunnelMultibinder.addBinding().to(HttpTunnel.class);
            String customTunnelClassName = EnvironmentUtils.getProperty(CUSTOM_TUNNEL_CLASS);

            if (customTunnelClassName != null && !customTunnelClassName.isEmpty()) {
                Class<? extends Tunnel> customTunnelClass = ClassUtils
                    .getCustomTunnelClass(customTunnelClassName);
                tunnelMultibinder.addBinding().to(customTunnelClass);
            }
        }
    }

}
