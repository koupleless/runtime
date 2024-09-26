package com.PomXmlModifier;

import com.auto_module_upgrade.PomXmlModifier.PomModifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class PomModifierTest {

    @Test
    void testProcessProjectPath(@TempDir Path tempDir) throws Exception {
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, "<project></project>");

        assertDoesNotThrow(() -> PomModifier.processProjectPath(tempDir.toString()));

        String content = Files.readString(pomFile);
        assertTrue(content.contains("<groupId>com.alipay.sofa.koupleless</groupId>"));
        assertTrue(content.contains("<artifactId>koupleless-app-starter</artifactId>"));
    }

    @org.junit.jupiter.api.AfterEach
    void cleanUp() {
        System.gc();
    }
}
