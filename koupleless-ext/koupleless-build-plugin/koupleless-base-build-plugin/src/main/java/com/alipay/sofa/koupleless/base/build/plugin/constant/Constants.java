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
package com.alipay.sofa.koupleless.base.build.plugin.constant;

import com.alipay.sofa.koupleless.base.build.plugin.utils.OSUtils;

import java.io.File;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: Constants.java, v 0.1 2024年06月26日 11:29 立蓬 Exp $
 */
public class Constants {
    /**
     * String Constants
     */
    public final static String COMMA_SPLIT                    = ",";

    public static final String FILE_PREFIX                    = OSUtils
        .getLocalFileProtocolPrefix();
    public static final String HTTP_PREFIX                    = "http://";

    public static final String HTTPS_PREFIX                   = "https://";

    /**
     * ark.conf
     */
    public final static String ARK_CONF_BASE_DIR              = "conf/ark";
    public final static String ARK_PROPERTIES_FILE            = "ark.properties";

    public final static String ARK_YML_FILE                   = "ark.yml";

    public final static String SOFA_ARK_MODULE                = "SOFA-ARK" + File.separator + "biz";

    /**
     * extension-config
     */
    public final static String EXTENSION_INTEGRATE_URLS       = "integrateBizURLs";

    public final static String EXTENSION_INTEGRATE_LOCAL_DIRS = "integrateLocalDirs";

}