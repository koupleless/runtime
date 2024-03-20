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
package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author CodeNoobKing
 * @date 2024/3/20
 **/
@RunWith(MockitoJUnitRunner.class)
public class ClassCopyStrategyTest {
    @InjectMocks
    private AdapterCopyService                   adapterCopyService;

    @Mock
    private ClassCopyStrategy                    classCopyStrategy;
    @Mock
    private MergeServiceDirectoryCopyStrategy    mergeServiceDirectoryCopyStrategy;
    @Mock
    private MergeSpringFactoryConfigCopyStrategy mergeSpringFactoryConfigCopyStrategy;

    @Test
    public void testCopy() throws Throwable {
        adapterCopyService.copy(null, "example/file0.class", new byte[0]);
        verify(classCopyStrategy, times(1)).copy(any(), any(), any());

        adapterCopyService.copy(null, "META-INF/services/file0", new byte[0]);
        verify(mergeServiceDirectoryCopyStrategy, times(1)).copy(any(), any(), any());

        adapterCopyService.copy(null, "META-INF/spring.factories", new byte[0]);
        verify(mergeSpringFactoryConfigCopyStrategy, times(1)).copy(any(), any(), any());
    }

}
