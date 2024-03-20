package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class CopyAdapterStrategyTest {

    @Test
    public void testDirectCopy() throws Throwable {
        File adapterFile = new File(getClass().getClassLoader().getResource("testcopy/file0").toURI());
        File buildDir = Files.createTempDirectory("test").toFile();
        ClassCopyStrategy directCopyStrategy = new ClassCopyStrategy();
        directCopyStrategy.copy(buildDir, "example/file0", Files.readAllBytes(adapterFile.toPath()));

        byte[] bytes = Files.readAllBytes(Paths.get(buildDir.toPath().toString(), "example", "file0"));
        Assert.assertEquals("hello world!", new String(bytes));
    }
}
