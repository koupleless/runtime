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

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.HttpTunnel;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
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

    static {
        Injector injector = Guice.createInjector(new TunnelGuiceModule());
        for (Binding<Tunnel> binding : injector.findBindingsByType(new TypeLiteral<Tunnel>() {
        })) {
            tunnelList.add(binding.getProvider().get());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.init(commandService);
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

    private static class TunnelGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<Tunnel> tunnelMultibinder = Multibinder.newSetBinder(binder(),
                Tunnel.class);
            tunnelMultibinder.addBinding().to(HttpTunnel.class);
            String customTunnelClass = EnvironmentUtils.getProperty(CUSTOM_TUNNEL_CLASS);
            if (!customTunnelClass.isEmpty()) {
                Class<?> tunnelClass;
                try {
                    tunnelClass = Class.forName(customTunnelClass);
                } catch (ClassNotFoundException e) {
                    throw new ArkletInitException(e);
                }
                if (!Tunnel.class.isAssignableFrom(tunnelClass)) {
                    throw new ArkletInitException(
                        "custom tunnel class didn't implement tunnel interface");
                }
                tunnelMultibinder.addBinding().to((Class<? extends Tunnel>) tunnelClass);
            }
        }
    }

}
