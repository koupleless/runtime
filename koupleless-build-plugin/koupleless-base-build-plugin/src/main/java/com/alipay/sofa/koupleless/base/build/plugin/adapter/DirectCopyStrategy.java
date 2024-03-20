package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class DirectCopyStrategy implements CopyAdapterStrategy {
    /**
     * directly copy the file from in to out
     */
    @Override
    public void copy(File adapter, File build) throws Throwable {
        InputStream is = Files.newInputStream(adapter.toPath());
        OutputStream os = Files.newOutputStream(build.toPath());
        IOUtils.copy(is, os);
    }
}
