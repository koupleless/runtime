package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class CopyAdapterStrategyTest {

    @Test
    public void testDirectCopy() throws Throwable {
        File adapterFile = new File(getClass().getClassLoader().getResource("testcopy/file0").toURI());
        File buildFile = Files.createTempFile("test", "copy").toFile();
        DirectCopyStrategy directCopyStrategy = new DirectCopyStrategy();
        directCopyStrategy.copy(adapterFile, buildFile);

        byte[] bytes = Files.readAllBytes(buildFile.toPath());
        Assert.assertEquals("hello world!", new String(bytes));
    }
}
