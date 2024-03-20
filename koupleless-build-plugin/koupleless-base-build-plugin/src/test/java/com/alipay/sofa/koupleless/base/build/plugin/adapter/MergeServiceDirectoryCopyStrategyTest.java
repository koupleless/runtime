package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeServiceDirectoryCopyStrategyTest {

    @Test
    public void testMergeServiceDirectory() throws Throwable {
        File buildDir = Files.createTempDirectory("testDir").toFile();
        File buildDirTemplate = new File(getClass().getClassLoader().getResource("testcopy/services0").toURI());
        File adapterDir = new File(getClass().getClassLoader().getResource("testcopy/services1").toURI());
        FileUtils.copyDirectory(buildDirTemplate, buildDir);

        new MergeServiceDirectoryCopyStrategy().copy(adapterDir, buildDir);

        List<String> fileNames = Arrays.asList(buildDir.list());
        System.out.println(fileNames);
        Assert.assertEquals(3, fileNames.size());
        Assert.assertTrue(fileNames.contains("org.apache.dubbo.common.context.FrameworkExt"));
        Assert.assertTrue(fileNames.contains("org.apache.dubbo.rpc.Filter"));
        Assert.assertTrue(fileNames.contains("org.foo.bar"));

        List<String> lines = Files.readAllLines(Paths.get(buildDir.getAbsolutePath(), "org.apache.dubbo.common.context.FrameworkExt"));
        List<String> expected = new ArrayList<>();
        expected.add("com.alipay.sofa.koupleless.support.dubbo.KouplelessConfigManager");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.KouplelessServiceRepository");

        Assert.assertEquals(expected, lines);

        lines = Files.readAllLines(Paths.get(buildDir.getAbsolutePath(), "org.apache.dubbo.rpc.Filter"));
        expected.clear();
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter0");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter1");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter2");
        Assert.assertEquals(expected, lines);
    }
}
