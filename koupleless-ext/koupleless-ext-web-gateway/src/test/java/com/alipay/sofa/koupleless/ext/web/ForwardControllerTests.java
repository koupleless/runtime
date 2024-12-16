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
package com.alipay.sofa.koupleless.ext.web;

import com.alibaba.fastjson.JSONArray;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.koupleless.common.util.ReflectionUtils;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.Forward;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.ForwardItems;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.Forwards;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.GatewayProperties;
import com.alipay.sofa.koupleless.ext.web.gateway.ForwardController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ForwardControllerTests {
    private String            baseConfPath = "base_forwards.yaml";

    private String            biz1ConfPath = "biz1_forwards.yaml";

    private String            biz2ConfPath = "biz2_forwards.yaml";

    @Mock
    private Biz               biz1;

    @Mock
    private Biz               biz2;

    @Mock
    private ClassLoader       biz1CL       = Mockito.mock(ClassLoader.class);

    @Mock
    private ClassLoader       biz2CL       = Mockito.mock(ClassLoader.class);

    @Mock
    private BizManagerService bizManagerService;

    @Before
    public void prepare() {
        ArkClient.setBizManagerService(bizManagerService);
        when(biz1.getIdentity()).thenReturn("biz1");
        when(biz2.getIdentity()).thenReturn("biz2");

        when(bizManagerService.getBizByClassLoader(biz1CL)).thenReturn(biz1);
        when(bizManagerService.getBizByClassLoader(biz2CL)).thenReturn(biz2);
    }

    @Test
    public void testRedirectForwards() throws IOException, ServletException {
        ForwardController controller = new ForwardController();
        ReflectionUtils.setField("baseForwards", controller, loadForwards(baseConfPath));
        ReflectionUtils.setField("bizForwards", controller, initBizForwards());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletContext baseContext = Mockito.mock(ServletContext.class);
        when(request.getServletContext()).thenReturn(baseContext);

        ServletContext context1 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher1 = Mockito.mock(RequestDispatcher.class);
        when(context1.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher1);

        ServletContext context2 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher2 = Mockito.mock(RequestDispatcher.class);
        when(context2.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher2);

        ServletContext context3 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher3 = Mockito.mock(RequestDispatcher.class);
        when(context3.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher3);

        when(baseContext.getContext(Mockito.anyString())).then((invocation) -> {
            String uri = invocation.getArgument(0);
            // test biz1 conf
            if (uri.startsWith("/test1/")) {
                return context1;
            }

            // test biz2 conf
            if (uri.startsWith("/test2/")) {
                return context2;
            }
            if (uri.startsWith("/test3/")) {
                return context3;
            }
            return baseContext;
        });

        // test biz1 conf
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test1.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher1, Mockito.times(1)).forward(request, response);

        // test biz2 conf
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test1.xxx.com/test2"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher2, Mockito.times(1)).forward(request, response);

        // test base conf
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test3.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher3, Mockito.times(1)).forward(request, response);

        // test no conf
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://test100.xxx.com/test1/xx"));
        Assert.assertThrows(ResponseStatusException.class,
            () -> controller.redirect(request, response));

        // test priority: biz conf > base conf
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test5.xxx.com/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher1, Mockito.times(2)).forward(request, response);
    }

    private Forwards loadForwards(String confPath) {
        Yaml yaml = new Yaml();
        JSONArray array = yaml.loadAs(
            ForwardControllerTests.class.getClassLoader().getResourceAsStream(confPath),
            JSONArray.class);

        GatewayProperties properties = new GatewayProperties();
        properties.setForwards(array.toJavaList(Forward.class));

        Forwards forwards = new Forwards();
        ForwardItems.init(forwards, properties);
        return forwards;
    }

    private Map<ClassLoader, Forwards> initBizForwards() {
        Map<ClassLoader, Forwards> res = new HashMap<>();
        res.put(biz1CL, loadForwards(biz1ConfPath));
        res.put(biz2CL, loadForwards(biz2ConfPath));
        return res;
    }
}