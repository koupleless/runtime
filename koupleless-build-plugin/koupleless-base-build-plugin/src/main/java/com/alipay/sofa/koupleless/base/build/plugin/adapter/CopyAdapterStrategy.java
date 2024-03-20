package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import java.io.File;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public interface CopyAdapterStrategy {
    void copy(File adapter, File build) throws Throwable;
}
