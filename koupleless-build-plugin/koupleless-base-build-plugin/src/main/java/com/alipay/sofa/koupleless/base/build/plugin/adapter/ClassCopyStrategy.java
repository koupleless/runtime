package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class ClassCopyStrategy implements CopyAdapterStrategy {
    /**
     * directly copy the file from in to out
     */
    @Override
    public void copy(File buildDir, String entryName, byte[] content) throws Throwable {
        File fileToCreate = Paths.get(buildDir.getAbsolutePath(), entryName).toFile();
        Files.createDirectories(fileToCreate.toPath().getParent());
        if (!fileToCreate.exists()) {
            Files.createFile(fileToCreate.toPath());
        }

        // byte to input stream
        Files.write(
                fileToCreate.toPath(),
                content,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
