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
    private AdapterCopyService adapterCopyService;

    @Mock
    private ClassCopyStrategy                    classCopyStrategy;
    @Mock
    private MergeServiceDirectoryCopyStrategy    mergeServiceDirectoryCopyStrategy;
    @Mock
    private MergeSpringFactoryConfigCopyStrategy mergeSpringFactoryConfigCopyStrategy;

    @Test
    public void testCopy() throws Throwable {
        adapterCopyService.copy(null, "example/file0.class", new byte[0]);
        verify(classCopyStrategy, times(1)).copy(
                any(), any(), any()
        );

        adapterCopyService.copy(null, "META-INF/services/file0", new byte[0]);
        verify(mergeServiceDirectoryCopyStrategy, times(1)).copy(
                any(), any(), any()
        );

        adapterCopyService.copy(null, "META-INF/spring.factories", new byte[0]);
        verify(mergeSpringFactoryConfigCopyStrategy, times(1)).copy(
                any(), any(), any()
        );
    }

}
