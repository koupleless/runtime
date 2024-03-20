package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import lombok.SneakyThrows;

import java.io.File;

/**
 * @author CodeNoobKing
 * @date 2024/3/20
 **/
public class AdapterCopyService {

    private ClassCopyStrategy                    classCopyStrategy                    = new ClassCopyStrategy();
    private MergeServiceDirectoryCopyStrategy    mergeServiceDirectoryCopyStrategy    = new MergeServiceDirectoryCopyStrategy();
    private MergeSpringFactoryConfigCopyStrategy mergeSpringFactoryConfigCopyStrategy = new MergeSpringFactoryConfigCopyStrategy();

    @SneakyThrows
    public void copy(File buildDir, String entryName, byte[] content) {
        if (entryName.endsWith(".class")) {
            classCopyStrategy.copy(buildDir, entryName, content);
        } else if (entryName.startsWith("META-INF/services")) {
            mergeServiceDirectoryCopyStrategy.copy(buildDir, entryName, content);
        } else if (entryName.equals("META-INF/spring.factories")) {
            mergeSpringFactoryConfigCopyStrategy.copy(buildDir, entryName, content);
        }
    }
}
