package com.alipay.sofa.koupleless.plugin.manager.handler.releaser;

import com.alipay.sofa.ark.spi.model.Biz;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author: poorpaper
 * @time: 2024/11/24 00:27
 *
 * Log4j2资源释放器
 */

public class Log4jResourceReleaser implements ResourceReleaser{

    private static final Logger LOGGER = getLogger(Log4jResourceReleaser.class);

    @Override
    public void doRelease(Biz biz) {
        try {
            ClassLoader bizClassLoader = biz.getBizClassLoader();
            LoggerContextFactory factory = LogManager.getFactory();
            if (factory instanceof Log4jContextFactory) {
                String ctxName = Integer.toHexString(System.identityHashCode(bizClassLoader));
                ContextSelector selector = ((Log4jContextFactory) factory).getSelector();
                for (LoggerContext context : selector.getLoggerContexts()) {
                    if (context.getName().equals(ctxName)) {
                        boolean stop = context.stop(300, TimeUnit.MILLISECONDS);
                        LOGGER.info("[Log4jResourceReleaser] Stop biz {}:{} logger context,result={}",
                                biz.getBizName(), biz.getBizVersion(), stop);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error(String.format("[Log4jResourceReleaser] Error happened when clean biz %s log4j cache",
                    biz.getIdentity()), t);
        }
    }
}
