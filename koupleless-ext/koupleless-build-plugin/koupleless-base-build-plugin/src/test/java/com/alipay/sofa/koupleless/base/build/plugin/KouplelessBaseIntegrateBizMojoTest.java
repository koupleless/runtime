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
package com.alipay.sofa.koupleless.base.build.plugin;

import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessIntegrateBizConfig;
import com.alipay.sofa.koupleless.base.build.plugin.utils.OSUtils;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URISyntaxException;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.SOFA_ARK_MODULE;
import static com.alipay.sofa.koupleless.utils.MockUtils.getResourceAsFile;
import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBaseIntegrateBizMojoTest.java, v 0.1 2024年06月27日 19:51 立蓬 Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class KouplelessBaseIntegrateBizMojoTest {

    @Test
    public void testInitKouplelessIntegrateBizConfig() throws URISyntaxException {
        KouplelessBaseIntegrateBizMojo mojo = new KouplelessBaseIntegrateBizMojo();
        mojo.baseDir = getResourceAsFile("mockBaseDir");
        mojo.initKouplelessIntegrateBizConfig();

        assertEquals(3, mojo.kouplelessIntegrateBizConfig.getFileURLs().size());
        assertEquals(4, mojo.kouplelessIntegrateBizConfig.getLocalDirs().size());
    }

    @Test
    public void testIntegrateBizToResource() throws Exception {
        KouplelessBaseIntegrateBizMojo mojo = new KouplelessBaseIntegrateBizMojo();
        mojo.outputDirectory = getResourceAsFile("mockOutputDirectory");
        FileUtils.cleanDirectory(mojo.outputDirectory);

        String bizFileURL = OSUtils.getLocalFileProtocolPrefix() + getResourceAsFile(
            "resourcesToCopy/biz2-bootstrap-0.0.1-SNAPSHOT-ark-biz.jar");
        String bizDir = getResourceAsFile("resourcesToCopy/staticDeployDir").getAbsolutePath();
        mojo.kouplelessIntegrateBizConfig = KouplelessIntegrateBizConfig.builder()
            .fileURLs(Sets.newHashSet(bizFileURL)).localDirs(Sets.newHashSet(bizDir)).build();
        mojo.integrateBizToResource();

        File targetDir =  new File(mojo.outputDirectory,SOFA_ARK_MODULE);
        assertEquals(2, targetDir.listFiles().length);
    }
}