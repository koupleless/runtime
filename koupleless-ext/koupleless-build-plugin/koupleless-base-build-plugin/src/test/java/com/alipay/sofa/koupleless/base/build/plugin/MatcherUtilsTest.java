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
package com.alipay.sofa.koupleless.base.build.plugin;

import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
//import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MatcherUtils.java, v 0.1 2024年11月21日 10:56 立蓬 Exp $
 */
public class MatcherUtilsTest {

    String                                    MAPPING_FILE            = "mockBaseDir/conf/ark/adapter-mapping-test.yaml";

    private KouplelessBaseBuildPrePackageMojo mojo                    = new KouplelessBaseBuildPrePackageMojo();

    KouplelessAdapterConfig                   kouplelessAdapterConfig = loadConfig();

    Collection<MavenDependencyAdapterMapping> adapterMappings         = CollectionUtils
        .emptyIfNull(kouplelessAdapterConfig.getAdapterMappings());

    public MatcherUtilsTest() throws IOException {
    }

    private KouplelessAdapterConfig loadConfig() throws IOException {
        InputStream mappingConfigIS = this.getClass().getClassLoader()
            .getResourceAsStream(MAPPING_FILE);

        Yaml yaml = new Yaml();
        return yaml.loadAs(mappingConfigIS, KouplelessAdapterConfig.class);
    }

    /**
     * test for adaptor: koupleless-adapter-apollo-1.6
     *     matcher:
     *       groupId: com.ctrip.framework.apollo
     *       artifactId: apollo-client
     *       versionRange: "(,)"
     *     adapter:
     *       artifactId: koupleless-adapter-apollo-1.6
     */

}