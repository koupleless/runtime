package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import com.alipay.sofa.koupleless.build.common.SpringUtils;
import com.google.common.base.Preconditions;
import jdk.internal.util.xml.impl.Input;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.plexus.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeSpringFactoryConfigCopyStrategy implements CopyAdapterStrategy {

    public void mergeSpringFactories(Map<String, List<String>> adapterConfig,
                                     Map<String, List<String>> buildConfig) {
        for (Map.Entry<String, List<String>> entry : adapterConfig.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if (buildConfig.containsKey(key)) {
                List<String> mergedValue = new ArrayList<>(buildConfig.get(key));
                // only add the new values
                mergedValue.addAll(CollectionUtils.subtract(value, buildConfig.get(key)));
                buildConfig.put(key, mergedValue);
            } else {
                buildConfig.put(key, value);
            }
        }
    }

    public List<String> formatSpringFactoryConfig(Map<String, List<String>> config) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : config.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            result.add(key + "=" + String.join(",", value));
        }
        return result;
    }

    @Override
    public void copy(File buildDir, String entryName, byte[] content) throws Throwable {
        File factoryFile = new File(Paths.get(buildDir.getAbsolutePath(), "META-INF", "spring.factories").toUri());
        if (!factoryFile.exists()) {
            Files.createDirectories(factoryFile.toPath().getParent());
            Files.createFile(factoryFile.toPath());
        }

        Map<String, List<String>> adapterConfig = SpringUtils.INSTANCE()
                .parseSpringFactoryConfig(new ByteArrayInputStream(content));

        InputStream buildIS = Files.newInputStream(factoryFile.toPath());
        Map<String, List<String>> buildConfig = SpringUtils.INSTANCE().parseSpringFactoryConfig(buildIS);

        mergeSpringFactories(adapterConfig, buildConfig);
        List<String> lines = formatSpringFactoryConfig(buildConfig);
        Files.write(factoryFile.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
