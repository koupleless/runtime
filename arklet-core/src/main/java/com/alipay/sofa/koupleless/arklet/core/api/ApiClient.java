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
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.HttpTunnel;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.spi.metadata.MetadataHook;
import com.alipay.sofa.koupleless.arklet.core.spi.metadata.MetadataHookImpl;
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

    private static final List<Tunnel> tunnelList          = new ArrayList<>(8);

    private final static String       CUSTOM_TUNNEL_CLASS = "koupleless.arklet.custom.tunnel.classname";

    @Inject
    private CommandService            commandService;

    private static MetadataHook       metadataHook;

    static {
        Injector injector = Guice.createInjector(new TunnelGuiceModule());
        for (Binding<Tunnel> binding : injector.findBindingsByType(new TypeLiteral<Tunnel>() {
        })) {
            tunnelList.add(binding.getProvider().get());
        }
        ServiceLoader<MetadataHook> serviceLoader = ServiceLoader.load(MetadataHook.class);

        for (MetadataHook hook : serviceLoader) {
            // 检测到用户提供的实现，直接返回
            if (!hook.getClass().equals(MetadataHookImpl.class)) {
                metadataHook = hook;
            }
        }
        metadataHook = new MetadataHookImpl();
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.init(commandService, metadataHook);
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

    public MetadataHook getMetadataHook() {
        return metadataHook;
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
