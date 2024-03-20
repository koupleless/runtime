package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import com.alipay.sofa.koupleless.build.common.SpringUtils;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/20
 **/
public class MergeSpringFactoryConfigCopyStrategyTest {

    @Test
    public void testCopy() throws Throwable {
        File buildTemplate = new File(
                getClass().getClassLoader().getResource("testcopy/services0/spring.factory").toURI()
        );
        File buildDir = Files.createTempDirectory("test").toFile();
        MergeSpringFactoryConfigCopyStrategy copyStrategy = new MergeSpringFactoryConfigCopyStrategy();
        copyStrategy.copy(
                buildDir,
                "META-INF/spring.factory",
                Files.readAllBytes(buildTemplate.toPath())
        );

        File adapterFile = new File(
                getClass().getClassLoader().getResource("testcopy/services1/spring.factory").toURI()
        );

        copyStrategy.copy(
                buildDir,
                "META-INF/spring.factories",
                Files.readAllBytes(adapterFile.toPath())
        );

        Map<String, List<String>> properties = SpringUtils.INSTANCE().parseSpringFactoryConfig(
                Files.newInputStream(
                        Paths.get(buildDir.toPath().toString(), "META-INF", "spring.factories")
                )
        );

        Assert.assertEquals(
                properties.get("just"),
                Lists.newArrayList("keep")
        );

        Assert.assertEquals(
                properties.get("hello"),
                Lists.newArrayList("world")
        );

        Assert.assertEquals(
                properties.get("key0"),
                Lists.newArrayList("org.example.0")
        );

        Assert.assertEquals(
                properties.get("key1"),
                Lists.newArrayList(
                        "org.example.0",
                        "org.example.1",
                        "org.example.2",
                        "org.example.3",
                        "org.example.4"
                )
        );

        Assert.assertEquals(
                properties.get("key2"),
                Lists.newArrayList(
                        "org.example.0",
                        "org.example.2",
                        "org.example.1"
                )
        );
    }
}
