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
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.log.KouplelessLogger;
import com.alipay.sofa.koupleless.common.util.ReflectionUtils;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.Forward;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.ForwardItems;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.Forwards;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.GatewayProperties;
import com.alipay.sofa.koupleless.ext.web.gateway.ForwardController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(MockitoJUnitRunner.class)
public class ForwardControllerTests {
    private String baseConfPath = "base_forwards.yaml";

    private String biz1ConfPath = "biz1_forwards.yaml";

    private String biz2ConfPath = "biz2_forwards.yaml";

    @Test
    public void testRedirectForwards() throws IOException, ServletException {
        ForwardController controller = new ForwardController();
        ReflectionUtils.setField("baseForwards", controller, loadForwards(baseConfPath));
        ReflectionUtils.setField("bizForwards", controller, initBizForwards());

        KouplelessLogger logger = Mockito.mock(KouplelessLogger.class);
        Mockito.doNothing().when(logger).info(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.any());
        ReflectionUtils.setField("LOGGER", controller, logger);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletContext baseContext = Mockito.mock(ServletContext.class);
        Mockito.when(request.getServletContext()).thenReturn(baseContext);

        ServletContext context1 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher1 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context1.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher1);

        ServletContext context2 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher2 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context2.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher2);

        ServletContext context3 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher3 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context3.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher3);

        Mockito.when(baseContext.getContext(Mockito.anyString())).then((invocation) -> {
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
        Mockito.when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://test1.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher1, Mockito.times(1)).forward(request, response);

        // test biz2 conf
        Mockito.when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://test1.xxx.com/test2"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher2, Mockito.times(1)).forward(request, response);

        // test base conf
        Mockito.when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://test3.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher3, Mockito.times(1)).forward(request, response);

        // test no conf
        Mockito.when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://test4.xxx.com/test1/xx"));
        Assert.assertThrows(ResponseStatusException.class,
            () -> controller.redirect(request, response));
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

    private Map<Biz, Forwards> initBizForwards() {
        Map<Biz, Forwards> res = new HashMap<>();

        Biz biz1 = Mockito.mock(Biz.class);
        Mockito.when(biz1.getIdentity()).thenReturn("biz1");
        res.put(biz1, loadForwards(biz1ConfPath));

        Biz biz2 = Mockito.mock(Biz.class);
        Mockito.when(biz2.getIdentity()).thenReturn("biz2");
        res.put(biz2, loadForwards(biz2ConfPath));

        return res;
    }
}