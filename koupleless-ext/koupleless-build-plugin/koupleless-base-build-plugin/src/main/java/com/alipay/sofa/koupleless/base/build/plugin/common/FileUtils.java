/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.base.build.plugin.common;

import java.io.File;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: FileUtils.java, v 0.1 2024年07月20日 09:06 立蓬 Exp $
 */
public class FileUtils {
    public static void createNewDirectory(File dir){
        if (dir.exists()) {
            org.apache.commons.io.FileUtils.deleteQuietly(dir);
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}