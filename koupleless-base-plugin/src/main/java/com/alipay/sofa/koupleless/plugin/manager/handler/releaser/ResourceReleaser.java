package com.alipay.sofa.koupleless.plugin.manager.handler.releaser;

import com.alipay.sofa.ark.spi.model.Biz;

/**
 * @author: poorpaper
 * @time: 2024/11/24 00:16
 *
 * 通用资源释放接口
 */

public interface ResourceReleaser {

    /**
     * 获取待释放资源上下文
     *
     * @param biz
     */
    void doRelease(Biz biz);
}
