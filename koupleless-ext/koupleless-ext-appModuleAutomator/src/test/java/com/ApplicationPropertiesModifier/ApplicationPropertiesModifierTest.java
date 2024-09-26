package com.ApplicationPropertiesModifier;

import com.auto_module_upgrade.ApplicationPropertiesModifier.ApplicationPropertiesModifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesModifierTest {

    @Test
    void testModifyApplicationProperties(@TempDir Path tempDir) throws Exception {
        String applicationName = "TestApp";
        Path propertiesFile = tempDir.resolve("application.properties");
        Files.writeString(propertiesFile, "some.property=value");

        assertDoesNotThrow(() -> ApplicationPropertiesModifier.modifyApplicationProperties(tempDir.toString(), applicationName));

        String content = Files.readString(propertiesFile);
        assertTrue(content.contains("spring.application.name=" + applicationName));
    }
}