package com.alipay.sofa.koupleless.base.build.plugin;

import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author CodeNoobKing
 * @date 2024/4/30
 **/
public class TestAdapterFile {

    private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

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
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("adapter-mapping.yaml");
        KouplelessAdapterConfig config = yamlMapper.readValue(is, KouplelessAdapterConfig.class);

        Assert.assertEquals(
                "koupleless-adapter-apollo-1.6",
                getMappingArtifact(config, "com.ctrip.framework.apollo:apollo-client:1.6.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-dubbo-2.6",
                getMappingArtifact(config, "com.alibaba:dubbo:2.6.1:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-dubbo-2.7",
                getMappingArtifact(config, "dubbo-spring-boot-starter:2.7.3:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-dubbo-2.7",
                getMappingArtifact(config, "com.alibaba:dubbo:2.7.3:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-dubbo-3.2",
                getMappingArtifact(config, "org.apache.dubbo:dubbo:3.2.1:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-dubbo-3.1",
                getMappingArtifact(config, "org.apache.dubbo:dubbo:3.1.1:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-3.0",
                getMappingArtifact(config, "spring-boot-starter-log4j2:3.0.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-3.0",
                getMappingArtifact(config, "spring-boot-starter-log4j2:3.1.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-3.2",
                getMappingArtifact(config, "spring-boot-starter-log4j2:3.2.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.1",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.1.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.1",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.2.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.1",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.3.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.4",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.4.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.4",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.5.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.4",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.6.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-log4j2-spring-starter-2.7",
                getMappingArtifact(config, "spring-boot-starter-log4j2:2.7.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-logback-spring-starter-3.0",
                getMappingArtifact(config, "spring-boot-starter-logging:3.0:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-logback-spring-starter-3.0",
                getMappingArtifact(config, "spring-boot-starter-logging:3.1:jar")
        );


        Assert.assertEquals(
                "koupleless-adapter-logback-spring-starter-3.2",
                getMappingArtifact(config, "spring-boot-starter-logging:3.2:jar")
        );

        Assert.assertEquals(
                "koupleless-adapter-logback",
                getMappingArtifact(config, "spring-boot-starter-logging:1.1:jar")
        );

    }
}
