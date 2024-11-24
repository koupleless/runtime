package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.koupleless.plugin.manager.handler.releaser.Log4jResourceReleaser;
import com.alipay.sofa.koupleless.plugin.manager.handler.releaser.ResourceReleaser;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author: poorpaper
 * @time: 2024/11/23 11:37
 *
 * 模块卸载后释放Log4j资源
 */
public class ResourceReleaseOnUninstallEventHandler implements EventHandler<BeforeBizRecycleEvent> {

    private static final Logger LOGGER = getLogger(ResourceReleaseOnUninstallEventHandler.class);

    static final ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, ResourceReleaser>> BIZ_CLASS_LOADER_TO_RELEASER =
            new ConcurrentHashMap<>();

    public ResourceReleaseOnUninstallEventHandler() {
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        BIZ_CLASS_LOADER_TO_RELEASER.putIfAbsent(contextClassLoader, new ConcurrentHashMap<>());
        // 注册资源释放器
        BIZ_CLASS_LOADER_TO_RELEASER.get(contextClassLoader).putIfAbsent(Log4jResourceReleaser.class.getName(), new Log4jResourceReleaser());
    }

    @Override
    public void handleEvent(BeforeBizRecycleEvent event) {
        ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
        LOGGER.info(
                "[ResourceReleaseOnUninstallEventHandler] Module name: {} , BizClassLoader: {} .",
                event.getSource().getBizName(), bizClassLoader);

        ConcurrentHashMap<String, ResourceReleaser> releasers = BIZ_CLASS_LOADER_TO_RELEASER.get(bizClassLoader);
        if (releasers == null) {
            LOGGER.info(
                    "[ResourceReleaseOnUninstallEventHandler] No releasers found for module: {} , just return. ",
                    event.getSource().getBizName());
            return;
        }

        LOGGER.info(
                "[ResourceReleaseOnUninstallEventHandler] {} releasers found for module: {} . ",
                releasers.size(), event.getSource().getBizName());
        for (ResourceReleaser releaser : releasers.values()) {
            releaser.doRelease(event.getSource());
        }
    }

    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }
}
