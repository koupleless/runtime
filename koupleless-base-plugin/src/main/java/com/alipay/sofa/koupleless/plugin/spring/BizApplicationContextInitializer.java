package com.alipay.sofa.koupleless.plugin.spring;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class BizApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
                .getBizRuntimeContextByClassLoader(classLoader);
        bizRuntimeContext.setRootApplicationContext(applicationContext);
    }
}
