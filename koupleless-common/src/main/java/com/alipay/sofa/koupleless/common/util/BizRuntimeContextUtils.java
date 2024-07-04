/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.springframework.context.ApplicationContext;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizRuntimeUtils.java, v 0.1 2024年07月04日 14:28 立蓬 Exp $
 */
public class BizRuntimeContextUtils {
    public static ApplicationContext getApplicationContext(Biz biz){
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
                .getRuntimeMap().get(biz.getBizClassLoader());
        if(null == bizRuntimeContext){
            return null;
        }
        return bizRuntimeContext.getRootApplicationContext();
    }
}