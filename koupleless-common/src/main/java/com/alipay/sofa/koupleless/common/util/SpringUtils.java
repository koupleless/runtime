/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.spi.model.Biz;
import org.springframework.context.ApplicationContext;

import static com.alipay.sofa.koupleless.common.util.BizRuntimeContextUtils.getApplicationContext;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: SpringUtils.java, v 0.1 2024年07月04日 14:31 立蓬 Exp $
 */
public class SpringUtils {
    public static boolean containsBean(Biz biz, String beanName){
        ApplicationContext bizContext = getApplicationContext(biz);
        return null != bizContext && bizContext.containsBean(beanName);
    }

    public static Object getBean(Biz biz, String beanName){
        if(containsBean(biz,beanName)){
            return getApplicationContext(biz).getBean(beanName);
        }
        return null;
    }
}