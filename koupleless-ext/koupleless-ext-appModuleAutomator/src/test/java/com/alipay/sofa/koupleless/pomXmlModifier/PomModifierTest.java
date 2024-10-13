/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.pomXmlModifier;

import com.alipay.sofa.koupleless.auto_module_upgrade.pomXmlModifier.PomModifier;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class PomModifierTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testProcessProjectPath() throws Exception {
        File projectDir = tempFolder.newFolder("testProject");
        File pomFile = new File(projectDir, "pom.xml");
        String initialPomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>demo-project</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "</project>";

        try (FileWriter writer = new FileWriter(pomFile)) {
            writer.write(initialPomContent);
        }

        String applicationName = "testApp";
        PomModifier.processProjectPath(projectDir.getAbsolutePath(), applicationName);

        // 读取修改后的 pom.xml 文件
        String updatedPomContent = new String(Files.readAllBytes(pomFile.toPath()));
        System.out.println("Updated POM content:");
        System.out.println(updatedPomContent);

        // 使用新的 SAXBuilder 实例重新读取文件
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(pomFile);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        // 验证关键元素是否存在
        Element properties = root.getChild("properties", ns);
        Element dependencies = root.getChild("dependencies", ns);
        Element build = root.getChild("build", ns);

        assertNotNull("properties 元素应该存在", properties);
        assertNotNull("dependencies 元素应该存在", dependencies);
        assertNotNull("build 元素应该存在", build);

        if (properties != null) {
            assertNotNull("sofa.ark.version 属性应该存在", properties.getChild("sofa.ark.version", ns));
            assertNotNull("koupleless.runtime.version 属性应该存在", properties.getChild("koupleless.runtime.version", ns));
        }

        if (dependencies != null) {
            assertTrue("应该添加 koupleless-app-starter 依赖", dependencies.getChildren("dependency", ns).stream()
                    .anyMatch(dep -> "com.alipay.sofa.koupleless".equals(dep.getChildText("groupId", ns)) &&
                            "koupleless-app-starter".equals(dep.getChildText("artifactId", ns))));
        }

        if (build != null) {
            Element plugins = build.getChild("plugins", ns);
            assertNotNull("plugins 元素应该存在", plugins);
            if (plugins != null) {
                assertTrue("应该添加 sofa-ark-maven-plugin", plugins.getChildren("plugin", ns).stream()
                        .anyMatch(plugin -> "com.alipay.sofa".equals(plugin.getChildText("groupId", ns)) &&
                                "sofa-ark-maven-plugin".equals(plugin.getChildText("artifactId", ns))));
                assertTrue("应该添加 spring-boot-maven-plugin", plugins.getChildren("plugin", ns).stream()
                        .anyMatch(plugin -> "org.springframework.boot".equals(plugin.getChildText("groupId", ns)) &&
                                "spring-boot-maven-plugin".equals(plugin.getChildText("artifactId", ns))));
            }
        }
    }

    @Test
    public void testCreateAndInitializePomFile() throws Exception {
        File projectDir = tempFolder.newFolder("emptyProject");
        File pomFile = new File(projectDir, "pom.xml");

        assertFalse("pom.xml 文件不应该存在", pomFile.exists());

        String applicationName = "newApp";
        PomModifier.processProjectPath(projectDir.getAbsolutePath(), applicationName);

        assertTrue("pom.xml 文件应该被创建", pomFile.exists());
        String content = new String(Files.readAllBytes(pomFile.toPath()));
        assertTrue("pom.xml 应该包含基本结构", content.contains("<project") && content.contains("</project>"));
    }

    @Test
    public void testHandleEdgeCases() throws Exception {
        File projectDir = tempFolder.newFolder("edgeCaseProject");
        File pomFile = new File(projectDir, "pom.xml");
        String edgeCasePomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>edge-case-project</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "</project>";

        try (FileWriter writer = new FileWriter(pomFile)) {
            writer.write(edgeCasePomContent);
        }

        String applicationName = "edgeApp";
        PomModifier.processProjectPath(projectDir.getAbsolutePath(), applicationName);

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(pomFile);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        // 打印整个 POM 文件内容以进行调试
        System.out.println("Updated edge case POM content:");
        new XMLOutputter().output(document, System.out);

        // 验证关键元素是否存在
        Element properties = root.getChild("properties", ns);
        Element dependencies = root.getChild("dependencies", ns);
        Element build = root.getChild("build", ns);

        // 使用软断言来收集所有的断言结果
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(properties).as("properties 元素").isNotNull();
        softly.assertThat(dependencies).as("dependencies 元素").isNotNull();
        softly.assertThat(build).as("build 元素").isNotNull();

        if (dependencies != null) {
            List<Element> dependencyList = dependencies.getChildren("dependency", ns);
            softly.assertThat(dependencyList).as("依赖列表").isNotEmpty();

            boolean hasKouplelessDependency = dependencyList.stream()
                    .anyMatch(dep -> "com.alipay.sofa.koupleless".equals(dep.getChildText("groupId", ns)) &&
                            "koupleless-app-starter".equals(dep.getChildText("artifactId", ns)));
            softly.assertThat(hasKouplelessDependency).as("koupleless-app-starter 依赖").isTrue();
        }

        if (build != null) {
            Element plugins = build.getChild("plugins", ns);
            softly.assertThat(plugins).as("plugins 元素").isNotNull();

            if (plugins != null) {
                List<Element> pluginList = plugins.getChildren("plugin", ns);
                softly.assertThat(pluginList).as("插件列表").isNotEmpty();

                boolean hasSofaArkPlugin = pluginList.stream()
                        .anyMatch(plugin -> "com.alipay.sofa".equals(plugin.getChildText("groupId", ns)) &&
                                "sofa-ark-maven-plugin".equals(plugin.getChildText("artifactId", ns)));
                softly.assertThat(hasSofaArkPlugin).as("sofa-ark-maven-plugin").isTrue();

                boolean hasSpringBootPlugin = pluginList.stream()
                        .anyMatch(plugin -> "org.springframework.boot".equals(plugin.getChildText("groupId", ns)) &&
                                "spring-boot-maven-plugin".equals(plugin.getChildText("artifactId", ns)));
                softly.assertThat(hasSpringBootPlugin).as("spring-boot-maven-plugin").isTrue();
            }
        }

        // 断言所有的软断言
        softly.assertAll();
    }
}
