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
package com.alipay.sofa.koupleless.plugin.spring;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.koupleless.common.util.LoaderUtil;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;


import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_EXPORT_CLASS_ENABLE;

/**
 * <p>ServerlessApplicationListener class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/10/30 9:38 下午
 * @version 1.0.0
 */
public class ServerlessApplicationListener implements ApplicationListener<SpringApplicationEvent>,
                                           Ordered {

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> restartClassLoaderClass = LoaderUtil.loadClass(
                "org.springframework.boot.devtools.restart.classloader.RestartClassLoader");
            if (restartClassLoaderClass.equals(currentClassLoader.getClass())) {
                Thread.currentThread().setContextClassLoader(currentClassLoader.getParent());
            }
        } catch (ClassNotFoundException e) {}
        if (ArkClient.class.getClassLoader() == Thread.currentThread().getContextClassLoader()) {
            if (event instanceof ApplicationStartingEvent) {
                // 开启ark2.0 embed
                ArkConfigs.setEmbedEnable(true);
                // 基础设施类强制委托
                System.setProperty(PLUGIN_EXPORT_CLASS_ENABLE, "true");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * 优先级要高于 ArkApplicationStartListener 否则，会提前进入 ark 1.0 分支
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
