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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.koupleless.arklet.core.health.model.BizHealthMeta;
import com.alipay.sofa.koupleless.arklet.core.health.model.PluginHealthMeta;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

import java.util.List;

/**
 * <p>BizInfoContributor class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArkBizInfoContributor.java, v 0.1 2024年03月19日 11:03 立蓬 Exp $
 */
public class BizInfoContributor implements InfoContributor {
    /** {@inheritDoc} */
    @Override
    public void contribute(Info.Builder builder) {
        List<Biz> bizList = ArkClient.getBizManagerService().getBizInOrder();
        List<BizHealthMeta> bizHealthMetaList = BizHealthMeta.createBizMetaList(bizList);

        List<Plugin> pluginList = ArkClient.getPluginManagerService().getPluginsInOrder();
        List<PluginHealthMeta> pluginHealthMetaList = PluginHealthMeta
            .createPluginMetaList(pluginList);
        builder.withDetail("arkBizInfo", bizHealthMetaList);
        builder.withDetail("arkPluginInfo", pluginHealthMetaList);
    }
}
