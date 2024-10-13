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
package com.alipay.sofa.koupleless.auto_module_upgrade.pomXmlModifier;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Namespace;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.List;

import com.alipay.sofa.koupleless.auto_module_upgrade.filterconfiguration.SlimmingConfiguration;

public class PomModifier {
    private static final Logger logger = LoggerFactory.getLogger(PomModifier.class);
    private static Properties config = new Properties();
    private static Namespace ns;

    static {
        try (java.io.InputStream input = PomModifier.class.getClassLoader().getResourceAsStream("config.properties")) {
            config.load(input);
        } catch (IOException ex) {
            logger.error("无法加载配置文件", ex);
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("请输入项目的绝对路径：");
            String projectPath = scanner.nextLine();
            System.out.println("请输入应用名称：");
            String applicationName = scanner.nextLine();
            processProjectPath(projectPath, applicationName);
        } catch (IOException | JDOMException ex) {
            logger.error("发生错误：", ex);
            System.out.println("处理项目时发生错误，请检查日志以获取详细信息。");
        }
    }

    public static void processProjectPath(String projectPath, String applicationName) throws IOException, JDOMException {
        File projectDirectory = new File(projectPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            logger.error("提供的项目路径不存在或不是目录");
            return;
        }
        File pomFile = new File(projectDirectory, "pom.xml");
        if (!pomFile.exists() || pomFile.length() == 0) {
            createAndInitializePomFile(pomFile);
        }
        updatePomFile(pomFile, applicationName);
    }

    private static void createAndInitializePomFile(File pomFile) throws IOException {
        String initialContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + "    <modelVersion>4.0.0</modelVersion>\n" + "    <groupId>com.example</groupId>\n" + "    <artifactId>demo-project</artifactId>\n" + "    <version>1.0-SNAPSHOT</version>\n" + "</project>";
        try (FileWriter writer = new FileWriter(pomFile)) {
            writer.write(initialContent);
            logger.info("pom.xml 文件已创建并初始化在: {}", pomFile.getAbsolutePath());
        }
    }

    private static void updatePomFile(File file, String applicationName) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(file);
        Element root = document.getRootElement();
        ns = root.getNamespace();

        updateProperties(root);
        updateDependencies(root);
        updateBuild(root, applicationName);

        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        try (FileWriter writer = new FileWriter(file)) {
            xmlOutput.output(document, writer);
        }
        logger.info("POM 文件已更新: {}", file.getAbsolutePath());
    }

    private static void updateProperties(Element root) {
        Element properties = root.getChild("properties", ns);
        if (properties == null) {
            properties = new Element("properties", ns);
            root.addContent(properties);
        }
        String sofaArkVersion = SlimmingConfiguration.getSofaArkVersion();
        String kouplelessRuntimeVersion = SlimmingConfiguration.getKouplelessRuntimeVersion();

        updateOrAddElement(properties, "sofa.ark.version", sofaArkVersion != null ? sofaArkVersion : "${sofa.ark.version}");
        updateOrAddElement(properties, "koupleless.runtime.version", kouplelessRuntimeVersion != null ? kouplelessRuntimeVersion : "${revision}");
    }

    private static void updateDependencies(Element root) {
        Element dependencies = getOrCreateElement(root, "dependencies");
        Element newDependency = new Element("dependency", ns);
        updateOrAddElement(newDependency, "groupId", "com.alipay.sofa.koupleless");
        updateOrAddElement(newDependency, "artifactId", "koupleless-app-starter");
        updateOrAddElement(newDependency, "version", "${koupleless.runtime.version}");

        // 检查是否已存在相同的依赖
        boolean exists = false;
        List<Element> existingDependencies = dependencies.getChildren("dependency", ns);
        for (Element dep : existingDependencies) {
            String depGroupId = dep.getChildText("groupId", ns);
            String depArtifactId = dep.getChildText("artifactId", ns);
            if ("com.alipay.sofa.koupleless".equals(depGroupId) && "koupleless-app-starter".equals(depArtifactId)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            dependencies.addContent(newDependency);
        }
    }

    private static void updateBuild(Element root, String applicationName) {
        Element build = getOrCreateElement(root, "build");
        Element plugins = getOrCreateElement(build, "plugins");
        addSofaArkPlugin(plugins, applicationName);
        addSpringBootPlugin(plugins);
    }

    private static Element getOrCreateElement(Element parent, String childName) {
        Element child = parent.getChild(childName, ns);
        if (child == null) {
            child = new Element(childName, ns);
            parent.addContent(child);
        }
        return child;
    }

    private static Element createPluginElement(String groupId, String artifactId, String version) {
        Element plugin = new Element("plugin", ns);
        updateOrAddElement(plugin, "groupId", groupId);
        updateOrAddElement(plugin, "artifactId", artifactId);
        if (version != null) {
            updateOrAddElement(plugin, "version", version);
        }
        return plugin;
    }

    private static void addSofaArkPlugin(Element plugins, String applicationName) {
        String groupId = "com.alipay.sofa";
        String artifactId = "sofa-ark-maven-plugin";

        // 检查是否已存在相同的插件
        Element existingPlugin = findExistingPlugin(plugins, groupId, artifactId);
        if (existingPlugin != null) {
            updateExistingPlugin(existingPlugin, applicationName);
        } else {
            Element newPlugin = createPluginElement(groupId, artifactId, "${sofa.ark.version}");
            addPluginConfiguration(newPlugin, applicationName);
            plugins.addContent(0, newPlugin);
        }
    }

    private static Element findExistingPlugin(Element plugins, String groupId, String artifactId) {
        List<Element> existingPlugins = plugins.getChildren("plugin", ns);
        for (Element plugin : existingPlugins) {
            String pluginGroupId = plugin.getChildText("groupId", ns);
            String pluginArtifactId = plugin.getChildText("artifactId", ns);
            if (groupId.equals(pluginGroupId) && artifactId.equals(pluginArtifactId)) {
                return plugin;
            }
        }
        return null;
    }

    private static void updateExistingPlugin(Element plugin, String applicationName) {
        // 更新现有插件的配置
        Element configuration = getOrCreateElement(plugin, "configuration");
        updateOrAddElement(configuration, "skipArkExecutable", "true");
        updateOrAddElement(configuration, "outputDirectory", "./target");
        updateOrAddElement(configuration, "bizName", applicationName);
        updateOrAddElement(configuration, "webContextPath", applicationName);
        updateOrAddElement(configuration, "declaredMode", "true");
    }

    private static void addPluginConfiguration(Element plugin, String applicationName) {
        Element executions = getOrCreateElement(plugin, "executions");
        Element execution = getOrCreateElement(executions, "execution");
        updateOrAddElement(execution, "id", "default-cli");

        Element goals = getOrCreateElement(execution, "goals");
        goals.addContent(new Element("goal", ns).setText("repackage"));

        Element configuration = getOrCreateElement(plugin, "configuration");
        updateOrAddElement(configuration, "skipArkExecutable", "true");
        updateOrAddElement(configuration, "outputDirectory", "./target");
        updateOrAddElement(configuration, "bizName", applicationName);
        updateOrAddElement(configuration, "webContextPath", applicationName);
        updateOrAddElement(configuration, "declaredMode", "true");
    }

    private static void addSpringBootPlugin(Element plugins) {
        String groupId = "org.springframework.boot";
        String artifactId = "spring-boot-maven-plugin";

        // 检查是否已存在相同的插件
        if (findExistingPlugin(plugins, groupId, artifactId) == null) {
            Element plugin = createPluginElement(groupId, artifactId, null);
            plugins.addContent(plugin);
        }
    }

    private static void updateOrAddElement(Element parent, String childName, String childValue) {
        Element child = parent.getChild(childName, ns);
        if (child == null) {
            child = new Element(childName, ns);
            parent.addContent(child);
        }
        child.setText(childValue);
    }
}