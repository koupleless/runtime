package com.alipay.sofa.koupleless.test.suite.common;

import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import lombok.Getter;

/**
 * @author CodeNoobKing
 * @date 2024/5/21
 **/
public class IntegrationLogger {

    @Getter
    static org.slf4j.Logger logger = null;

    static {
        logger = ArkletLoggerFactory.getLogger("INTEGRATION-TEST");
    }

}
