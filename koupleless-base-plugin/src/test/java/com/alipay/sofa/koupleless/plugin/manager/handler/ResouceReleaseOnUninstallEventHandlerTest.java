package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;

/**
 * @author: poorpaper
 * @time: 2024/11/24 16:11
 */
public class ResouceReleaseOnUninstallEventHandlerTest {

    private ClassLoader originalClassLoader = currentThread().getContextClassLoader();

    private LoggerContext testLoggerContext;

    @Before
    public void setUp() {
        currentThread().setContextClassLoader(originalClassLoader);
    }

    private void generateTestLoggerContextFactory() {
        System.setProperty("log4j2.loggerContextFactory", "org.apache.logging.log4j.core.impl.Log4jContextFactory");
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        // 获取LoggerContextFactory
        LoggerContextFactory factory = LogManager.getFactory();
        // 创建一个logger
        org.apache.logging.log4j.Logger logger = LogManager.getLogger();
        // 获取ContextSelector
        ContextSelector selector = ((Log4jContextFactory) factory).getSelector();
        // 获取LoggerContext
        testLoggerContext = selector.getLoggerContexts().get(0);

        // 启动LoggerContext
        if (testLoggerContext.isInitialized()) {
            testLoggerContext.start();
        }
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void testHandleRelease() throws IOException {
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(originalClassLoader);
        BeforeBizRecycleEvent beforeBizRecycleEvent = new BeforeBizRecycleEvent(bizModel);

        generateTestLoggerContextFactory();

        assertEquals(true, testLoggerContext.isStarted() || testLoggerContext.isStarting());

        new ResourceReleaseOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        assertEquals(true, testLoggerContext.isStopped() || testLoggerContext.isStopping());
    }
}
