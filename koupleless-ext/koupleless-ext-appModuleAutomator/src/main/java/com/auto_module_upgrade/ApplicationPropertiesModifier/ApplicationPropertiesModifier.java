package com.auto_module_upgrade.ApplicationPropertiesModifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationPropertiesModifier {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertiesModifier.class);

    public static void modifyApplicationProperties(String directoryPath, String applicationName) throws IOException {
        Path directory = Paths.get(directoryPath);
        scanDirectory(directory, applicationName);
    }

    private static void scanDirectory(Path directory, String applicationName) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    scanDirectory(path, applicationName);
                } else if (path.getFileName().toString().equalsIgnoreCase("application.properties")) {
                    modifySpringApplicationName(path, applicationName);
                } else if (Files.isDirectory(path) && path.getFileName().toString().equalsIgnoreCase("resources")) {
                    createApplicationProperties(path, applicationName);
                }
            }
        }
    }

    private static void modifySpringApplicationName(Path path, String applicationName) throws IOException {
        List<String> lines = Files.readAllLines(path);
        boolean exists = lines.stream().anyMatch(line -> line.trim().startsWith("spring.application.name"));

        if (!exists) {
            lines.add("spring.application.name=" + applicationName);
        } else {
            lines = lines.stream()
                    .map(line -> line.startsWith("spring.application.name") ? "spring.application.name=" + applicationName : line)
                    .collect(Collectors.toList());
        }

        Files.write(path, lines);
        logger.info("已修改 application.properties: {}", path);
    }

    private static void createApplicationProperties(Path directory, String applicationName) throws IOException {
        Path newPropsFile = directory.resolve("application.properties");
        if (!Files.exists(newPropsFile)) {
            Files.createFile(newPropsFile);
            Files.write(newPropsFile, List.of("spring.application.name=" + applicationName));
            logger.info("已创建并初始化 application.properties: {}", newPropsFile);
        }
    }
}