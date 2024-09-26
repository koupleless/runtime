package com.auto_module_upgrade.Filterconfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SlimmingConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SlimmingConfiguration.class);

    public static void createBootstrapProperties(String targetDirectoryPath, String fileName) {
        Map<String, String> completeConfigs = new LinkedHashMap<>();
        completeConfigs.put("excludeGroupIds", "org.springframework,aopalliance*,asm*,cglib*,com.alibaba.common.lang*,com.alibaba.common.resourcebundle*,com.alibaba.tbase*," +
                "com.alipay*,com.antcloud.antvip*,com.caucho.hessian*,com.caucho*,com.ctc.wstx*,com.fasterxml*,com.google.code*," +
                "com.google.common*,com.google.gson*,com.google.guava*,com.google.http-client*,com.google.inject*," +
                "com.google.protobuf*,com.ibatis*,com.iwallet.biz*,com.lmax*,com.taobao.config*,com.taobao.hsf*," +
                "com.taobao.notify*,com.taobao.remoting*,com.taobao.tair*,groovy*,io.fury*,io.grpc*,io.mosn.layotto*," +
                "io.netty*,io.openmessaging*,io.prometheus*,javax*,javax.el*,javax.script*,javax.servlet*," +
                "javax.validation*,loccs-bcprov*,log4j*,mysql*,net.sf.acegisecurity*,net.sf.cglib*,netty*," +
                "ognl*,org.aopalliance*,org.apache*,org.aspectj*,org.codehaus*,org.codehaus.groovy*," +
                "org.codehaus.xfire*,org.dom4j*,org.hibernate.validator*,org.junit*,org.mvel2*,org.mybatis*," +
                "org.mybatis.spring*,org.mybatis.spring.boot.autoconfigure*,org.projectlombok*,org.quartz*,org.reflections*," +
                "org.slf4j*,org.springframework*,org.yaml*,xerces*,xml-apis*,xpp3*,jakarta*,org.latencyutils*," +
                "org.hdrhistogram*,io.micrometer*,ch.qos.logback*,com.squareup.okhttp3*,com.squareup.okhttp*," +
                "net.sf.ehcache*,redis.clients*");
        completeConfigs.put("excludeArtifactIds", "sofa-ark-spi,commons-lang,commons-collections,commons-httpclient,commons-io");

        Path directory = Paths.get(targetDirectoryPath);
        Path propertiesFile = directory.resolve(fileName);

        try {
            Files.createDirectories(directory);
            logger.info("目录已创建: {}", targetDirectoryPath);

            List<String> existingLines = Files.exists(propertiesFile) ? Files.readAllLines(propertiesFile) : new ArrayList<>();
            Map<String, String> existingConfigs = parseExistingConfigs(existingLines);

            List<String> updatedLines = updateConfigs(existingConfigs, completeConfigs);

            Files.write(propertiesFile, updatedLines);
            logger.info("配置文件已更新: {}", propertiesFile);
        } catch (IOException e) {
            logger.error("创建或更新配置文件时发生错误", e);
        }
    }

    private static Map<String, String> parseExistingConfigs(List<String> lines) {
        Map<String, String> configs = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts.length > 1 ? parts[1].trim() : "";
                configs.put(key, value);
            }
        }
        return configs;
    }

    private static String mergeConfigValues(String existingValue, String completeValue) {
        Set<String> items = new LinkedHashSet<>();
        if (!existingValue.isEmpty()) {
            items.addAll(Arrays.asList(existingValue.split(",")));
        }
        items.addAll(Arrays.asList(completeValue.split(",")));
        return String.join(",", items);
    }

    private static List<String> updateConfigs(Map<String, String> existingConfigs, Map<String, String> completeConfigs) {
        List<String> updatedLines = new ArrayList<>();
        for (Map.Entry<String, String> entry : completeConfigs.entrySet()) {
            String key = entry.getKey();
            String completeValue = entry.getValue();
            String existingValue = existingConfigs.getOrDefault(key, "");

            String updatedValue = mergeConfigValues(existingValue, completeValue);
            updatedLines.add(key + "=" + updatedValue);
        }
        return updatedLines;
    }
}