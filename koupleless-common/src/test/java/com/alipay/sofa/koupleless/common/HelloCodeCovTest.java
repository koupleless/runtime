package com.alipay.sofa.koupleless.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author CodeNoobKing
 * @date 2024/4/2
 **/
public class HelloCodeCovTest {

    @Test
    public void testHelloCodeCov() {
        HelloCodeCov helloCodeCov = new HelloCodeCov();
        try {
            helloCodeCov.justTriggerACodeCoverage();
        } catch (Throwable t) {
            Assert.fail(t.getMessage());
        }
    }
}
