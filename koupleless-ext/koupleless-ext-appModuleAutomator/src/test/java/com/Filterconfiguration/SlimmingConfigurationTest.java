package com.Filterconfiguration;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.auto_module_upgrade.Filterconfiguration.SlimmingConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SlimmingConfigurationTest {

    private Path testDirectory;
    private String targetDirectory;
    private String fileName = "bootstrap.properties";

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试目录
        testDirectory = Files.createTempDirectory("testDirectory");
        targetDirectory = testDirectory.toString();
    }

    @AfterEach
    void tearDown() throws IOException {
        // 删除测试目录及其内容
        Files.walk(testDirectory)
                .sorted((p, n) -> -p.compareTo(n))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void testCreateBootstrapProperties_DirectoryCreated() {
        // 调用方法创建bootstrap.properties文件
        SlimmingConfiguration.createBootstrapProperties(targetDirectory, fileName);

        // 验证目录是否被创建
        File directory = new File(targetDirectory);
        assertTrue(directory.exists(), "The target directory should be created.");
    }

    @Test
    void testCreateBootstrapProperties_FileCreatedAndWritten() throws IOException {
        // 调用方法创建bootstrap.properties文件
        SlimmingConfiguration.createBootstrapProperties(targetDirectory, fileName);

        // 验证文件是否被创建
        File propertiesFile = new File(targetDirectory, fileName);
        assertTrue(propertiesFile.exists(), "bootstrap.properties 文件应该被创建。");

        // 读取文件内容
        String actualContent = new String(Files.readAllBytes(propertiesFile.toPath()));

        // 验证文件内容是否正确
        assertTrue(actualContent.contains("excludeGroupIds="), "文件应包含 excludeGroupIds 配置");
        assertTrue(actualContent.contains("excludeArtifactIds="), "文件应包含 excludeArtifactIds 配置");
        assertFalse(actualContent.contains("excludes="), "文件不应包含 excludes 配置");

        // 验证 excludeGroupIds 的内容
        assertTrue(actualContent.contains("org.springframework,"), "excludeGroupIds 应包含 org.springframework");
        assertTrue(actualContent.contains("aopalliance*,"), "excludeGroupIds 应包含 aopalliance*");
        assertTrue(actualContent.contains("com.google.guava*,"), "excludeGroupIds 应包含 com.google.guava*");

        // 验证 excludeArtifactIds 的内容
        assertTrue(actualContent.contains("sofa-ark-spi,"), "excludeArtifactIds 应包含 sofa-ark-spi");
        assertTrue(actualContent.contains("commons-lang,"), "excludeArtifactIds 应包含 commons-lang");
        assertTrue(actualContent.contains("commons-collections,"), "excludeArtifactIds 应包含 commons-collections");

        // 验证每行的格式
        String[] lines = actualContent.split(System.lineSeparator());
        for (String line : lines) {
            assertTrue(line.matches("^[a-zA-Z]+=.*$"), "每行应该是 'key=value' 格式");
        }
    }

    @Test
    void testCreateBootstrapProperties(@TempDir Path tempDir) {
        String targetDir = tempDir.toString();
        String fileName = "bootstrap.properties";

        assertDoesNotThrow(() -> SlimmingConfiguration.createBootstrapProperties(targetDir, fileName));

        Path propertiesFile = tempDir.resolve(fileName);
        try {
            assertTrue(Files.exists(propertiesFile));
            assertTrue(Files.size(propertiesFile) > 0);
        } catch (IOException e) {
            fail("无法访问或读取属性文件: " + e.getMessage());
        }
    }
}