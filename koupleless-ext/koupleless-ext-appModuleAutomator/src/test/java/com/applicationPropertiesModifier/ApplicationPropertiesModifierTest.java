package com.applicationPropertiesModifier;

import com.auto_module_upgrade.applicationPropertiesModifier.ApplicationPropertiesModifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesModifierTest {

    @TempDir
    Path tempDir;

    @Test
    void testModifyApplicationProperties() throws Exception {
        String applicationName = "TestApp";
        Path propertiesFile = tempDir.resolve("application.properties");
        Files.write(propertiesFile, "some.property=value".getBytes(StandardCharsets.UTF_8));

        ApplicationPropertiesModifier.modifyApplicationProperties(tempDir.toString(), applicationName);

        List<String> lines = Files.readAllLines(propertiesFile, StandardCharsets.UTF_8);
        assertTrue(lines.contains("spring.application.name=" + applicationName), "应该添加 spring.application.name 属性");
        assertTrue(lines.contains("some.property=value"), "不应修改现有属性");
    }
}