package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import com.alipay.sofa.koupleless.build.common.SpringUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
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

    /**
     * 合并 spring factory 配置文件。
     * 由于 spring factory 配置文件是一个特殊的配置文件，需要合并而不是直接覆盖。
     * spring factory 文件由 key value 组成，需要合并。
     * 当 key 重复时，需要合并 value。
     * 当 key 不存在时，直接添加。
     */
    @Override
    public void copy(File adapter, File build) throws Throwable {
        FileInputStream adapterIS = FileUtils.openInputStream(adapter);
        if (!build.exists()) {
            Preconditions.checkState(
                    build.createNewFile(),
                    "outputServiceFile %s should be created successfully",
                    build.toPath().toAbsolutePath()
            );
            IOUtils.copy(adapterIS, FileUtils.openOutputStream(build));
        }

        FileInputStream buildIS = FileUtils.openInputStream(build);
        Map<String, List<String>> inFactories = SpringUtils.INSTANCE().parseSpringFactoryConfig(adapterIS);
        Map<String, List<String>> outFactories = SpringUtils.INSTANCE().parseSpringFactoryConfig(buildIS);
        mergeSpringFactories(inFactories, outFactories);
        List<String> lines = formatSpringFactoryConfig(outFactories);
        Files.write(build.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
