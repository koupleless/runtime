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
package com.alipay.sofa.koupleless.arklet.core.component;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.health.custom.MockBizManagerService;
import com.alipay.sofa.koupleless.arklet.core.ops.BatchInstallHelper;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationServiceImpl;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * @author mingmen
 * @since 2023/10/26
 */

@RunWith(MockitoJUnitRunner.class)
public class UnifiedOperationServiceImplTests {
    @InjectMocks
    private UnifiedOperationServiceImpl unifiedOperationService;

    @Spy
    private BatchInstallHelper          batchInstallHelper;

    @Before
    public void setUp() {
        if (unifiedOperationService == null) {
            unifiedOperationService = new UnifiedOperationServiceImpl();
        }
    }

    /**
     * 测试初始化方法
     */
    @Test
    public void testInit() {
        unifiedOperationService.init();
    }

    /**
     * 测试销毁方法
     */
    @Test
    public void testDestroy() {
        unifiedOperationService.destroy();
    }

    /**
     * 测试安装方法，输入合法URL
     */
    @Test
    public void testInstallWithValidUrl() throws Throwable {
        try (MockedStatic<ArkClient> arkClientMockedStatic = Mockito.mockStatic(ArkClient.class)) {
            ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
            arkClientMockedStatic
                .when(() -> ArkClient.installOperation(Mockito.any(BizOperation.class),
                    Mockito.any(String[].class), Mockito.anyMap()))
                .thenReturn(clientResponse);
            arkClientMockedStatic.when(ArkClient::getBizManagerService)
                .thenReturn(new MockBizManagerService());

            ClientResponse response = unifiedOperationService.install("testBiz1", "bizVersion",
                "http://example.com/biz.jar", new String[] {}, new HashMap<>(), true);
            arkClientMockedStatic
                .verify(() -> ArkClient.installOperation(Mockito.any(BizOperation.class),
                    Mockito.any(String[].class), Mockito.anyMap()));
            Assert.assertEquals(clientResponse, response);
        }
    }

    /**
     * 测试卸载方法，输入合法的bizName和bizVersion
     */
    @Test
    public void testUninstallWithValidBizNameAndVersion() throws Throwable {
        try (MockedStatic<ArkClient> arkClientMockedStatic = Mockito.mockStatic(ArkClient.class)) {
            ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
            arkClientMockedStatic
                .when(() -> ArkClient.uninstallBiz(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(clientResponse);
            ClientResponse response = unifiedOperationService.uninstall("bizName", "1.0.0");
            arkClientMockedStatic
                .verify(() -> ArkClient.uninstallBiz(Mockito.anyString(), Mockito.anyString()));
            Assert.assertEquals(clientResponse, response);
        }
    }

    /**
     * 测试切换Biz方法，输入合法的bizName和bizVersion
     */
    @Test
    public void testSwitchBizWithValidBizNameAndVersion() throws Throwable {
        try (MockedStatic<ArkClient> arkClientMockedStatic = Mockito.mockStatic(ArkClient.class)) {
            ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
            arkClientMockedStatic
                .when(() -> ArkClient.switchBiz(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(clientResponse);
            ClientResponse response = unifiedOperationService.switchBiz("bizName", "1.0.0");
            arkClientMockedStatic
                .verify(() -> ArkClient.switchBiz(Mockito.anyString(), Mockito.anyString()));
            Assert.assertEquals(clientResponse, response);
        }
    }

    @SneakyThrows
    @Test
    public void testBatchInstall() {
        {
            List<String> paths = new ArrayList<>();
            paths.add("/file/a-biz.jar");
            paths.add("/file/b-biz.jar");
            paths.add("/file/notbiz.jar");
            Map<Integer, List<String>> pathsInOrder = new HashMap<>();
            pathsInOrder.put(100, paths);

            doReturn(pathsInOrder).when(batchInstallHelper).getBizUrlsFromLocalFileSystem(any());

            doReturn(new HashMap<>()).when(batchInstallHelper).getMainAttributes(anyString());
        }

        BatchInstallResponse response = unifiedOperationService
            .batchInstall(BatchInstallRequest.builder().bizDirAbsolutePath("/path/to/biz").build());

        Assert.assertTrue(response.getBizUrlToResponse().containsKey("/file/a-biz.jar"));

        Assert.assertTrue(response.getBizUrlToResponse().containsKey("/file/b-biz.jar"));

    }
}
