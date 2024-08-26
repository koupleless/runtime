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
package com.alipay.sofa.koupleless.common.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * @author yuanyuan
 * @since 2023/9/26 9:40 下午
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringServiceAndBeanFinderTest {

    @Mock
    private Biz               masterBiz;

    @Mock
    private Biz               biz1;

    @Mock
    private Biz               biz2;

    @Mock
    private Biz               biz3;

    @Mock
    private BizManagerService bizManagerService;

    @Before
    public void prepare() {
        ConfigurableApplicationContext masterCtx = buildApplicationContext("masterBiz");
        masterCtx.getBeanFactory().registerSingleton("baseBean", new BaseBean());

        ConfigurableApplicationContext bizCtx1 = buildApplicationContext("biz1");
        bizCtx1.getBeanFactory().registerSingleton("moduleBean", new ModuleBean());
        bizCtx1.getBeanFactory().registerSingleton("duplicatedBean", new DuplicatedBean());

        ConfigurableApplicationContext bizCtx3 = buildApplicationContext("biz3");
        bizCtx3.getBeanFactory().registerSingleton("moduleBean3", new ModuleBean());
        bizCtx3.getBeanFactory().registerSingleton("duplicatedBean", new DuplicatedBean());

        ArkClient.setBizManagerService(bizManagerService);

        ArkClient.setMasterBiz(masterBiz);
        when(bizManagerService.getBiz("master", "1.0.0")).thenReturn(masterBiz);
        when(masterBiz.getBizState()).thenReturn(BizState.ACTIVATED);
        when(masterBiz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        when(masterBiz.getBizName()).thenReturn("master");
        when(masterBiz.getBizVersion()).thenReturn("1.0.0");
        BizRuntimeContext masterBizRuntime = new BizRuntimeContext(masterBiz, masterCtx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(masterBizRuntime);

        when(bizManagerService.getBiz("biz1")).thenReturn(Collections.singletonList(biz1));
        when(bizManagerService.getActiveBiz("biz1")).thenReturn(biz1);
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        when(biz1.getBizClassLoader()).thenReturn(new URLClassLoader(new URL[0]));
        when(biz1.getBizName()).thenReturn("biz1");
        when(biz1.getBizVersion()).thenReturn("version1");
        BizRuntimeContext bizRuntime = new BizRuntimeContext(biz1, bizCtx1);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntime);

        when(bizManagerService.getBiz("biz3", "version1")).thenReturn(biz3);
        when(biz3.getBizState()).thenReturn(BizState.ACTIVATED);
        when(biz3.getBizClassLoader()).thenReturn(new URLClassLoader(new URL[0]));
        when(biz3.getBizName()).thenReturn("biz3");
        when(biz3.getBizVersion()).thenReturn("version1");
        BizRuntimeContext bizRuntime3 = new BizRuntimeContext(biz3, bizCtx3);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntime3);

        when(bizManagerService.getBizInOrder())
            .thenReturn(Lists.asList(masterBiz, new Biz[] { biz1, biz3 }));
    }

    @Test
    public void testSpringServiceInvoker() {
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(moduleBean);
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(null);
        Exception exception = Assert.assertThrows(BizRuntimeException.class,
            () -> moduleBean.test());
        Assert.assertEquals("biz biz1:version1 does not exist when called", exception.getMessage());

        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.RESOLVED);
        Exception exception1 = Assert.assertThrows(BizRuntimeException.class,
            () -> moduleBean.test());
        Assert.assertEquals("biz biz1:version1 is still installing when called",
            exception1.getMessage());
    }

    @Test
    public void testSpringServiceInvokerWithLazyInit() {
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(null);

        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        Assert.assertEquals("module", moduleBean.test());
    }

    @Test
    public void testSpringServiceFinder() {
        BaseBean baseBean = SpringServiceFinder.getBaseService("baseBean", BaseBean.class);
        Assert.assertNotNull(baseBean);
        BaseBean baseBean1 = SpringServiceFinder.getBaseService(BaseBean.class);
        Assert.assertNotNull(baseBean1);
        Map<String, BaseBean> baseBeanMap = SpringServiceFinder.listBaseServices(BaseBean.class);
        Assert.assertNotNull(baseBeanMap);
        Assert.assertEquals(1, baseBeanMap.size());
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(moduleBean);
        ModuleBean moduleBean1 = SpringServiceFinder.getModuleService("biz1", "version1",
            ModuleBean.class);
        Assert.assertNotNull(moduleBean1);
        Map<String, ModuleBean> moduleBeanMap = SpringServiceFinder.listModuleServices("biz1",
            "version1", ModuleBean.class);
        Assert.assertNotNull(moduleBeanMap);
        Assert.assertEquals(1, moduleBeanMap.size());

        Map<Biz, Map<String, ModuleBean>> allModuleBeanMap = SpringServiceFinder
            .listAllModuleServices(ModuleBean.class);
        Assert.assertNotNull(allModuleBeanMap);
        Assert.assertEquals(2, allModuleBeanMap.size());
        Assert.assertEquals(1, allModuleBeanMap.get(biz1).size());

        Assert.assertEquals("base", baseBean.test());
        Assert.assertEquals("module", moduleBean.test());

        // test to invoke crossing classloader
        URL url = SpringServiceAndBeanFinderTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        Object newModuleBean = null;
        try {
            Class<?> aClass = loader.loadClass(
                "com.alipay.sofa.koupleless.common.api.SpringServiceAndBeanFinderTest$ModuleBean");
            newModuleBean = aClass.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        ConfigurableApplicationContext biz2Ctx = buildApplicationContext("biz2");
        biz2Ctx.getBeanFactory().registerSingleton("moduleBean", newModuleBean);
        Mockito.when(bizManagerService.getBiz("biz2", "version1")).thenReturn(biz2);
        Mockito.when(biz2.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(biz2.getBizClassLoader()).thenReturn(loader);
        Mockito.when(biz2.getBizName()).thenReturn("biz2");
        BizRuntimeContext biz2Runtime = new BizRuntimeContext(biz2, biz2Ctx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(biz2Runtime);
        ModuleBean foundModuleBean = SpringServiceFinder.getModuleService("biz2", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(foundModuleBean);
        Assert.assertEquals(newModuleBean.toString(), foundModuleBean.toString());
        Assert.assertEquals("module", foundModuleBean.test());
    }

    // test with expected exception
    @Test
    public void testSpringServiceFinderWithoutBiz() {
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.UNRESOLVED);
        Exception exception1 = Assert.assertThrows(BizRuntimeException.class, () -> {
            SpringServiceFinder.getModuleService("biz1", "version1", "moduleBean",
                ModuleBean.class);
        });
        Assert.assertEquals("biz biz1:version1 state unresolved is not valid",
            exception1.getMessage());

        Object newModuleBean = null;
        URL url = SpringServiceAndBeanFinderTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        try {
            Class<?> aClass = loader.loadClass(
                "com.alipay.sofa.koupleless.common.api.SpringServiceAndBeanFinderTest$ModuleBean");
            newModuleBean = aClass.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        Mockito.when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        ConfigurableApplicationContext biz1Ctx = buildApplicationContext("biz1");
        biz1Ctx.getBeanFactory().registerSingleton("moduleBean", newModuleBean);
        BizRuntimeContext biz1Runtime = new BizRuntimeContext(biz1, biz1Ctx);
        biz1Runtime.setRootApplicationContext(null);
        BizRuntimeContextRegistry.registerBizRuntimeManager(biz1Runtime);
        Exception exception2 = Assert.assertThrows(BizRuntimeException.class, () -> {
            SpringServiceFinder.getModuleService("biz1", "version1", "moduleBean",
                ModuleBean.class);
        });
        Assert.assertEquals("biz biz1:version1 application context is null",
            exception2.getMessage());
    }

    @Test
    public void testSpringServiceLazyInit() {
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(null);
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(moduleBean);
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        ModuleBean moduleBean1 = SpringServiceFinder.getModuleService("biz1", "version1",
            ModuleBean.class);
        Assert.assertNotNull(moduleBean1);

        // test to invoke crossing classloader
        URL url = SpringServiceAndBeanFinderTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        Object newModuleBean = null;
        try {
            Class<?> aClass = loader.loadClass(
                "com.alipay.sofa.koupleless.common.api.SpringServiceAndBeanFinderTest$ModuleBean");
            newModuleBean = aClass.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }

        ConfigurableApplicationContext biz1Ctx = buildApplicationContext("biz1");
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        biz1Ctx.getBeanFactory().registerSingleton("moduleBean", newModuleBean);
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(biz1.getBizClassLoader()).thenReturn(loader);
        Mockito.when(biz1.getBizName()).thenReturn("biz1");
        BizRuntimeContext biz1Runtime = new BizRuntimeContext(biz1, biz1Ctx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(biz1Runtime);
        Assert.assertEquals("module", moduleBean.test());
    }

    @Test
    public void testArkAutowired() {
        ModuleBean moduleBean = new ModuleBean();
        ArkAutowiredBeanPostProcessor arkAutowiredBeanPostProcessor = new ArkAutowiredBeanPostProcessor();
        Object testBean = arkAutowiredBeanPostProcessor.postProcessBeforeInitialization(moduleBean,
            "moduleBean");
        Assert.assertNotNull(testBean);
        Assert.assertEquals(moduleBean, testBean);
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBean")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanList")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanSet")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanMap")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "moduleBean")),
            testBean));
    }

    @Test
    public void testCrossSerialize() {
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(null);
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(moduleBean);
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);

        // test to invoke crossing classloader
        URL url = SpringServiceAndBeanFinderTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        Object newModuleBean = null;
        try {
            Class<?> aClass = loader.loadClass(
                "com.alipay.sofa.koupleless.common.api.SpringServiceAndBeanFinderTest$ModuleBean");
            newModuleBean = aClass.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }

        ConfigurableApplicationContext biz1Ctx = buildApplicationContext("biz1");
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        biz1Ctx.getBeanFactory().registerSingleton("moduleBean", newModuleBean);
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(biz1.getBizClassLoader()).thenReturn(loader);
        Mockito.when(biz1.getBizName()).thenReturn("biz1");
        BizRuntimeContext biz1Runtime = new BizRuntimeContext(biz1, biz1Ctx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(biz1Runtime);

        ModuleBean moduleBean1 = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertEquals("test model name",
            moduleBean1.crossInvoker(new Model("test model name")));
    }

    @Test
    public void testGetActivatedModuleServices() {
        Map<Biz, ModuleBean> moduleBeanList = SpringServiceFinder
            .getActivatedModuleServices("moduleBean", ModuleBean.class);
        Assert.assertEquals(1, moduleBeanList.size());

        Map<Biz, ModuleBean> moduleBean2List = SpringServiceFinder
            .getActivatedModuleServices("moduleBean2", ModuleBean.class);
        Assert.assertTrue(moduleBean2List.isEmpty());

        Map<Biz, ModuleBean> moduleBean3List = SpringServiceFinder
            .getActivatedModuleServices("moduleBean3", ModuleBean.class);
        Assert.assertEquals(1, moduleBean3List.size());

        Map<Biz, Model> moduleList = SpringServiceFinder.getActivatedModuleServices("model1",
            Model.class);
        Assert.assertTrue(moduleList.isEmpty());

        Map<Biz, DuplicatedBean> duplicatedBeanList = SpringServiceFinder
            .getActivatedModuleServices("duplicatedBean", DuplicatedBean.class);

        Assert.assertEquals(2, duplicatedBeanList.size());
    }

    @Test
    public void testGetModuleServices() {
        Map<Biz, ModuleBean> moduleBeanList = SpringServiceFinder.getModuleServices("moduleBean",
            ModuleBean.class);
        Assert.assertEquals(1, moduleBeanList.size());

        Map<Biz, ModuleBean> moduleBean2List = SpringServiceFinder.getModuleServices("moduleBean2",
            ModuleBean.class);
        Assert.assertTrue(moduleBean2List.isEmpty());

        Map<Biz, ModuleBean> moduleBean3List = SpringServiceFinder.getModuleServices("moduleBean3",
            ModuleBean.class);
        Assert.assertEquals(1, moduleBean3List.size());

        Map<Biz, Model> moduleList = SpringServiceFinder.getModuleServices("model1", Model.class);
        Assert.assertTrue(moduleList.isEmpty());

        Map<Biz, DuplicatedBean> duplicatedBeanList = SpringServiceFinder
            .getModuleServices("duplicatedBean", DuplicatedBean.class);

        Assert.assertEquals(2, duplicatedBeanList.size());
    }

    @Test
    public void testGetModuleServiceWithoutVersion() {
        Assert.assertNotNull(SpringServiceFinder.getModuleServiceWithoutVersion("biz1",
            "moduleBean", ModuleBean.class));
        Exception exception = Assert.assertThrows(RuntimeException.class, () -> {
            SpringServiceFinder.getModuleServiceWithoutVersion("biz1", "moduleBean2",
                ModuleBean.class);
        });
        Assert.assertTrue(exception instanceof NoSuchBeanDefinitionException);
    }

    @Test
    public void testActivatedGetModuleServiceWithoutVersion() {
        Assert.assertNotNull(SpringServiceFinder.getActivatedModuleServiceWithoutVersion("biz1",
            "moduleBean", ModuleBean.class));
        Exception exception = Assert.assertThrows(RuntimeException.class, () -> {
            SpringServiceFinder.getActivatedModuleServiceWithoutVersion("biz1", "moduleBean2",
                ModuleBean.class);
        });
        Assert.assertTrue(exception instanceof NoSuchBeanDefinitionException);

        Exception exception1 = Assert.assertThrows(RuntimeException.class, () -> {
            SpringServiceFinder.getActivatedModuleServiceWithoutVersion("biz6", "moduleBean2",
                ModuleBean.class);
        });
        Assert.assertTrue(exception1 instanceof BizRuntimeException);
    }

    @Test
    public void testGetBaseBean() {
        Object baseBean = SpringBeanFinder.getBaseBean("baseBean");
        BaseBean baseBean1 = SpringBeanFinder.getBaseBean(BaseBean.class);
        Assert.assertNotNull(baseBean);
        Assert.assertNotNull(baseBean1);
    }

    public static ConfigurableApplicationContext buildApplicationContext(String appName) {
        Properties properties = new Properties();
        properties.setProperty("spring.application.name", appName);
        SpringApplication springApplication = new SpringApplication(CustomConfiguration.class);
        springApplication.setDefaultProperties(properties);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        return springApplication.run();
    }

    @Configuration
    public static class CustomConfiguration {

    }

    public static class Model {
        private String name;

        public Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BaseBean {
        public String test() {
            return "base";
        }
    }

    public static class DuplicatedBean {
    }

    public static class ModuleBean {

        @AutowiredFromBase
        private BaseBean              baseBean;

        @AutowiredFromBase
        private List<BaseBean>        baseBeanList;

        @AutowiredFromBase
        private Set<BaseBean>         baseBeanSet;

        @AutowiredFromBase
        private Map<String, BaseBean> baseBeanMap;

        @AutowiredFromBiz(bizName = "biz1", bizVersion = "version1")
        private ModuleBean            moduleBean;

        public String test() {
            return "module";
        }

        public String crossInvoker(Model model) {
            return model.getName();
        }
    }
}
