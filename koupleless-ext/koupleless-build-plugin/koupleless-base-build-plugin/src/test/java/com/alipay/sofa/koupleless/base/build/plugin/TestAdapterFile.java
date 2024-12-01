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
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @author CodeNoobKing
 * @since 2024/4/30
 **/
public class TestAdapterFile {

    //private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    String getMappingArtifact(KouplelessAdapterConfig config, String targetArtifactId) {
        for (MavenDependencyAdapterMapping mapping : config.getAdapterMappings()) {
            if (targetArtifactId.matches(mapping.getMatcher().getRegexp())) {
                return mapping.getAdapter().getArtifactId();
            }
        }
        return null;
    }

    @Test
    public void testAdapterFile() throws Exception {
        InputStream is = this.getClass().getClassLoader()
            .getResourceAsStream("adapter-mapping.yaml");
        //KouplelessAdapterConfig config = yamlMapper.readValue(is, KouplelessAdapterConfig.class);

        Yaml yaml = new Yaml();
        KouplelessAdapterConfig config = yaml.loadAs(is, KouplelessAdapterConfig.class);

        Assert.assertEquals("koupleless-adapter-apollo-1.6",
            getMappingArtifact(config, "com.ctrip.framework.apollo:apollo-client:1.6.0:jar"));

        Assert.assertEquals("koupleless-adapter-dubbo-2.6",
            getMappingArtifact(config, "com.alibaba:dubbo:2.6.1:jar"));

        Assert.assertEquals("koupleless-adapter-dubbo-2.7",
            getMappingArtifact(config, "dubbo-spring-boot-starter:2.7.3:jar"));

        Assert.assertEquals("koupleless-adapter-dubbo-2.7",
            getMappingArtifact(config, "com.alibaba:dubbo:2.7.3:jar"));

        Assert.assertEquals("koupleless-adapter-dubbo-3.2",
            getMappingArtifact(config, "org.apache.dubbo:dubbo:3.2.1:jar"));

        Assert.assertEquals("koupleless-adapter-dubbo-3.1",
            getMappingArtifact(config, "org.apache.dubbo:dubbo:3.1.1:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-3.0",
            getMappingArtifact(config, "spring-boot-starter-log4j2:3.0.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-3.0",
            getMappingArtifact(config, "spring-boot-starter-log4j2:3.1.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-3.2",
            getMappingArtifact(config, "spring-boot-starter-log4j2:3.2.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.1",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.1.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.1",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.2.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.1",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.3.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.4",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.4.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.4",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.5.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.4",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.6.0:jar"));

        Assert.assertEquals("koupleless-adapter-log4j2-spring-starter-2.7",
            getMappingArtifact(config, "spring-boot-starter-log4j2:2.7.0:jar"));

        Assert.assertEquals("koupleless-adapter-logback-spring-starter-3.0",
            getMappingArtifact(config, "spring-boot-starter-logging:3.0:jar"));

        Assert.assertEquals("koupleless-adapter-logback-spring-starter-3.0",
            getMappingArtifact(config, "spring-boot-starter-logging:3.1:jar"));

        Assert.assertEquals("koupleless-adapter-logback-spring-starter-3.2",
            getMappingArtifact(config, "spring-boot-starter-logging:3.2:jar"));

        Assert.assertEquals("koupleless-adapter-logback",
            getMappingArtifact(config, "spring-boot-starter-logging:1.1:jar"));

    }
}
