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
package com.alipay.sofa.koupleless.arklet.core.command.coordinate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for BizOpsPodCoordinator
 *
 * @author liuzhuoheng
 * @since 2025/7/15
 * @version 1.0.0
 */
public class BizOpsPodCoordinatorTest {

    @Before
    public void setUp() throws Exception {
        // 清理静态Map，确保测试隔离
        clearBizIdentityLockMap();
    }

    @After
    public void tearDown() throws Exception {
        // 清理静态Map，确保测试隔离
        clearBizIdentityLockMap();
    }

    /**
     * 通过反射清理静态Map
     */
    @SuppressWarnings("unchecked")
    private void clearBizIdentityLockMap() throws Exception {
        Field field = BizOpsPodCoordinator.class.getDeclaredField("bizIdentityLockMap");
        field.setAccessible(true);
        Map<String, String> map = (Map<String, String>) field.get(null);
        map.clear();
    }

    /**
     * 测试save方法 - 正常情况
     */
    @Test
    public void testSave_Normal() {
        String bizIdentity = "user-service:1.0.0-SNAPSHOT";
        String bizModelVersion = "default/user-service-abc123def456-xyz78";

        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

        // 验证通过canAccess方法来检查是否保存成功
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
    }

    /**
     * 测试save方法 - 使用真实的业务数据格式
     */
    @Test
    public void testSave_RealBusinessFormat() {
        String bizIdentity = "biz1-web-single-host:0.0.1-SNAPSHOT";
        String bizModelVersion = "default/biz1-web-single-host-786dfc476f-rt28q";

        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

        // 验证通过canAccess方法来检查是否保存成功
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
    }

    /**
     * 测试save方法 - bizModelVersion为null
     */
    @Test
    public void testSave_NullBizModelVersion() {
        String bizIdentity = "payment-service:2.1.0-SNAPSHOT";
        String bizModelVersion = null;

        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

        // 验证null会被转换为空字符串
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, ""));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, null));
    }

    /**
     * 测试save方法 - bizModelVersion为空字符串
     */
    @Test
    public void testSave_EmptyBizModelVersion() {
        String bizIdentity = "order-service:1.5.3-SNAPSHOT";
        String bizModelVersion = "";

        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, ""));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, null));
    }

    /**
     * 测试save方法 - 覆盖已存在的数据
     */
    @Test
    public void testSave_Override() {
        String bizIdentity = "inventory-service:3.2.1-SNAPSHOT";
        String bizModelVersion1 = "default/inventory-service-7f8d9c2e1a-old98";
        String bizModelVersion2 = "default/inventory-service-7f8d9c2e1a-new12";

        // 先保存第一个版本
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion1);
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion1));

        // 覆盖为第二个版本
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion2);
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion2));
        Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion1));
    }

    /**
     * 测试remove方法 - 正常情况
     */
    @Test
    public void testRemove_Normal() {
        String bizIdentity = "notification-service:0.8.9-SNAPSHOT";
        String bizModelVersion = "default/notification-service-5a6b7c8d9e-f0123";

        // 先保存
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));

        // 删除
        BizOpsPodCoordinator.remove(bizIdentity, bizModelVersion);

        // 验证删除后可以访问任何版本（因为Map中不存在该key）
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
        Assert.assertTrue(
            BizOpsPodCoordinator.canAccess(bizIdentity, "default/any-other-pod-version"));
    }

    /**
     * 测试remove方法 - 删除不存在的数据
     */
    @Test
    public void testRemove_NotExists() {
        String bizIdentity = "auth-service:2.0.5-SNAPSHOT";
        String bizModelVersion = "default/auth-service-9e8d7c6b5a-4f321";

        // 直接删除不存在的数据，不应该抛异常
        BizOpsPodCoordinator.remove(bizIdentity, bizModelVersion);

        // 验证可以正常访问
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
    }

    /**
     * 测试remove方法 - 版本不匹配
     */
    @Test
    public void testRemove_VersionMismatch() {
        String bizIdentity = "product-service:1.3.7-SNAPSHOT";
        String bizModelVersion1 = "default/product-service-4d5e6f7g8h-i9j0k";
        String bizModelVersion2 = "default/product-service-1a2b3c4d5e-f6g7h";

        // 保存版本1
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion1);

        // 尝试删除版本2（不匹配）
        BizOpsPodCoordinator.remove(bizIdentity, bizModelVersion2);

        // 验证版本1仍然存在
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion1));
        Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion2));
    }

    /**
     * 测试canAccess方法 - bizModelVersion为null或空
     */
    @Test
    public void testCanAccess_NullOrEmptyBizModelVersion() {
        String bizIdentity = "config-service:0.5.2-SNAPSHOT";

        // 无论Map中是否存在数据，null或空的bizModelVersion都应该返回true
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, null));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, ""));

        // 先保存一个版本
        BizOpsPodCoordinator.save(bizIdentity, "default/config-service-8h9i0j1k2l-m3n4o");

        // null或空的bizModelVersion仍然应该返回true
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, null));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, ""));
    }

    /**
     * 测试canAccess方法 - 不同版本的业务模块
     */
    @Test
    public void testCanAccess_DifferentBizVersions() {
        String bizIdentity1 = "biz1-web-single-host:0.0.1-SNAPSHOT";
        String bizIdentity2 = "biz1-web-single-host:0.0.2-SNAPSHOT";
        String podVersion1 = "default/biz1-web-single-host-6d66bd6955-p59v8";
        String podVersion2 = "default/biz1-web-single-host-786dfc476f-rt28q";

        // 保存不同版本的业务模块
        BizOpsPodCoordinator.save(bizIdentity1, podVersion1);
        BizOpsPodCoordinator.save(bizIdentity2, podVersion2);

        // 验证各自的版本访问权限
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity1, podVersion1));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity2, podVersion2));

        // 验证交叉访问权限（应该失败）
        Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity1, podVersion2));
        Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity2, podVersion1));
    }

    /**
     * 测试canAccess方法 - Map中对应的value为空
     */
    @Test
    public void testCanAccess_EmptyValueInMap() {
        String bizIdentity = "gateway-service:4.1.0-SNAPSHOT";
        String bizModelVersion = "default/gateway-service-6c7d8e9f0a-b1c2d";

        // 保存空版本
        BizOpsPodCoordinator.save(bizIdentity, "");

        // 无论传入什么版本都应该返回true（因为Map中的值为空）
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
        Assert
            .assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, "default/another-gateway-pod"));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, ""));
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, null));
    }

    /**
     * 测试canAccess方法 - 版本匹配
     */
    @Test
    public void testCanAccess_VersionMatch() {
        String bizIdentity = "log-service:2.3.4-SNAPSHOT";
        String bizModelVersion = "default/log-service-a1b2c3d4e5-f6g7h";

        // 保存版本
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

        // 相同版本应该返回true
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
    }

    /**
     * 测试canAccess方法 - 版本不匹配
     */
    @Test
    public void testCanAccess_VersionMismatch() {
        String bizIdentity = "search-service:1.8.2-SNAPSHOT";
        String bizModelVersion1 = "default/search-service-e5f6g7h8i9-j0k1l";
        String bizModelVersion2 = "default/search-service-m2n3o4p5q6-r7s8t";

        // 保存版本1
        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion1);

        // 版本2应该返回false
        Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion2));
    }

    /**
     * 测试canAccess方法 - bizIdentity不存在于Map中
     */
    @Test
    public void testCanAccess_BizIdentityNotExists() {
        String bizIdentity = "metrics-service:0.7.1-SNAPSHOT";
        String bizModelVersion = "default/metrics-service-u9v0w1x2y3-z4a5b";

        // 不存在的bizIdentity应该返回true
        Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion));
    }

    /**
     * 测试并发场景下的线程安全性
     */
    @Test
    public void testConcurrency() throws InterruptedException {
        int threadCount = 50;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String bizIdentity = "concurrent-service-" + threadIndex
                                             + ":1.0.0-SNAPSHOT";
                        String bizModelVersion = "default/concurrent-service-" + threadIndex
                                                 + "-pod-" + j;

                        // 保存
                        BizOpsPodCoordinator.save(bizIdentity, bizModelVersion);

                        // 检查访问权限
                        BizOpsPodCoordinator.canAccess(bizIdentity, bizModelVersion);

                        // 删除
                        BizOpsPodCoordinator.remove(bizIdentity, bizModelVersion);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        executor.shutdown();
        Assert.assertTrue("并发测试超时", latch.await(30, TimeUnit.SECONDS));

        // 验证没有抛出异常，并且最终状态正确
        Assert.assertTrue("并发测试完成", executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    /**
     * 测试多个不同bizIdentity的并发操作
     */
    @Test
    public void testMultipleBizIdentityConcurrency() throws InterruptedException {
        int bizCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(bizCount);
        CountDownLatch latch = new CountDownLatch(bizCount);

        for (int i = 0; i < bizCount; i++) {
            final String bizIdentity = "multi-service-" + i + ":1.0.0-SNAPSHOT";
            executor.execute(() -> {
                try {
                    // 保存
                    BizOpsPodCoordinator.save(bizIdentity, "default/multi-service-pod-v1-abc123");

                    // 验证可以访问
                    Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity,
                        "default/multi-service-pod-v1-abc123"));

                    // 更新版本
                    BizOpsPodCoordinator.save(bizIdentity, "default/multi-service-pod-v2-def456");

                    // 验证新版本可以访问，旧版本不能访问
                    Assert.assertTrue(BizOpsPodCoordinator.canAccess(bizIdentity,
                        "default/multi-service-pod-v2-def456"));
                    Assert.assertFalse(BizOpsPodCoordinator.canAccess(bizIdentity,
                        "default/multi-service-pod-v1-abc123"));

                } finally {
                    latch.countDown();
                }
            });
        }

        executor.shutdown();
        Assert.assertTrue("多Biz并发测试超时", latch.await(10, TimeUnit.SECONDS));
    }
}
