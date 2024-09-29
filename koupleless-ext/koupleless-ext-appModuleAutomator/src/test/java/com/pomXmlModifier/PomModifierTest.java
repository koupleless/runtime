package com.pomXmlModifier;

import com.auto_module_upgrade.pomXmlModifier.PomModifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class PomModifierTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testProcessProjectPath() throws Exception {
        File projectDir = tempFolder.newFolder("testProject");
        File pomFile = new File(projectDir, "pom.xml");
        String initialPomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + "    <modelVersion>4.0.0</modelVersion>\n" + "    <groupId>com.example</groupId>\n" + "    <artifactId>demo-project</artifactId>\n" + "    <version>1.0-SNAPSHOT</version>\n" + "</project>";

        try (FileWriter writer = new FileWriter(pomFile)) {
            writer.write(initialPomContent);
        }

        PomModifier.processProjectPath(projectDir.getAbsolutePath());

        String updatedPomContent = new String(Files.readAllBytes(pomFile.toPath()));
        assertTrue("应该添加 properties 节点", updatedPomContent.contains("<properties>"));
        assertTrue("应该添加 dependencies 节点", updatedPomContent.contains("<dependencies>"));
        assertTrue("应该添加 build 节点", updatedPomContent.contains("<build>"));
    }
}