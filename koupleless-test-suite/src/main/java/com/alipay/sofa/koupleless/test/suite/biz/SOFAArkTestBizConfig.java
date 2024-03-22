package com.alipay.sofa.koupleless.test.suite.biz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URLClassLoader;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/21
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SOFAArkTestBizConfig {
    private String         bootstrapClassName;
    private String         bizName;
    private String         bizVersion;
    private List<String>   testClassNames;
    private List<String>   includeClassPatterns;
    private URLClassLoader baseClassLoader;
    private List<String>   preFindResourceUrlKeyWords;
}
