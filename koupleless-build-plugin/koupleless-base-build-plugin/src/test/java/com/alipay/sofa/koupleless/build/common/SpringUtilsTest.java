package com.alipay.sofa.koupleless.build.common;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class SpringUtilsTest {

    private static SpringUtils INSTANCE = new SpringUtils();

    @Test
    public void testParseSpringFactoryConfig() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("spring.factory.example");
        Map<String, List<String>> keyToImpls = INSTANCE.parseSpringFactoryConfig(inputStream);

        ArrayList<String> expectedImpls = new ArrayList<>();
        expectedImpls.add("org.example.0");
        expectedImpls.add("org.example.1");
        expectedImpls.add("org.example.2");

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("key0", expectedImpls);
        expected.put("key1", expectedImpls);
        expected.put("key2", expectedImpls);

        Assert.assertEquals(expected, keyToImpls);
    }
}
