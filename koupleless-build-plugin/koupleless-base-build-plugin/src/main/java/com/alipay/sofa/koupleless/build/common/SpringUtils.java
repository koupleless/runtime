package com.alipay.sofa.koupleless.build.common;

import jdk.internal.util.xml.impl.Input;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class SpringUtils {

    private static SpringUtils instance = new SpringUtils();

    public static SpringUtils INSTANCE() {
        return instance;
    }

    @SneakyThrows
    public Map<String, List<String>> parseSpringFactoryConfig(InputStream inputStream) {
        Map<String, List<String>> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder currentLine = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().endsWith("\\")) {
                    currentLine.append(line.substring(0, line.length() - 1)); // Remove '\'
                } else {
                    currentLine.append(line);
                    String[] keyValue = currentLine.toString().split("=", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String[] values = keyValue[1].trim().split("\\s*,\\s*"); // Split on comma with optional spaces
                        result.put(key, (List<String>) Arrays.asList(values));
                    }
                    currentLine = new StringBuilder();
                }
            }
        }
        return result;
    }
}
