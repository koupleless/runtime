/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.arklet.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ResourceUtils.java, v 0.1 2024年12月18日 15:32 立蓬 Exp $
 */
public class ResourceUtils {
    public static MemoryPoolMXBean getMetaSpaceMXBean() {
        MemoryPoolMXBean metaSpaceMXBean = null;
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans)
            if (memoryPoolMXBean.getName().equals("Metaspace"))
                metaSpaceMXBean = memoryPoolMXBean;
        return metaSpaceMXBean;
    }
}