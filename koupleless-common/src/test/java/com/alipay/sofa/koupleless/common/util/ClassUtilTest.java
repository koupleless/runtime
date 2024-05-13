/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ClassUtilTest.java, v 0.1 2024年05月14日 00:05 立蓬 Exp $
 */
public class ClassUtilTest {
    @Test
    public void testSetField(){
        TestClassA testClassA = new TestClassA();
        ClassUtil.setField( "name", testClassA,"b");
        ClassUtil.setField( "parentName", testClassA,"child");
        assertEquals("b",testClassA.getName());
        assertEquals("child",testClassA.getParentName());
    }

    class TestClassParent{
        private String parentName = "parent";

        String getParentName() {
            return parentName;
        }
    }

    class TestClassA extends TestClassParent{
        private String name = "a";

        String getName() {
            return name;
        }
    }
}