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
package com.alipay.sofa.koupleless.utils;

import com.alipay.sofa.koupleless.base.build.plugin.model.ArtifactItem;
import com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Set;
import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MavenUtils.java, v 0.1 2024年07月24日 17:35 立蓬 Exp $
 */
public class MavenUtilsTest {
    private MavenProject bootstrapProject = getMockBootstrapProject();

    public MavenUtilsTest() throws URISyntaxException {
    }

    @Test
    public void testGetAllBundleArtifact() {
        Set<ArtifactItem> artifactItems = MavenUtils.getAllBundleArtifact(bootstrapProject);
        assertEquals(3, artifactItems.size());
    }

    private MavenProject getMockBootstrapProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-bootstrap");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("jar");
        project.setFile(CommonUtils.getResourceFile("mockBaseDir/base-bootstrap/pom.xml"));
        project.setParent(getRootProject());
        return project;
    }

    private MavenProject getRootProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("pom");
        project.setFile(CommonUtils.getResourceFile("mockBaseDir/pom.xml"));
        project.setParent(null);
        project.setModel(MavenUtils.buildPomModel(project.getFile()));
        return project;
    }
}