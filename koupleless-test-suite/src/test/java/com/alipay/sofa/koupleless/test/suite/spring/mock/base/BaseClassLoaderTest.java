package com.alipay.sofa.koupleless.test.suite.spring.mock.base;

import com.alipay.sofa.koupleless.test.suite.spring.base.BaseClassLoader;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/28
 **/
public class BaseClassLoaderTest {

    @Test
    public void testGetUrlsFromSurefireManifest() {
        URL url = getClass().getClassLoader().getResource("surefirebooter.jar");
        List<URL> urlsFromSurefireManifest = BaseClassLoader.getUrlsFromSurefireManifest(url);
        Assert.assertEquals(200, urlsFromSurefireManifest.size());
    }
}
