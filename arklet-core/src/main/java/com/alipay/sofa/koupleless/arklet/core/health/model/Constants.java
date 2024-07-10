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
package com.alipay.sofa.koupleless.arklet.core.health.model;

/**
 * <p>Constants class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
public class Constants {

    /** Constant <code>SYSTEM="system"</code> */
    public static final String SYSTEM                = "system";

    /** Constant <code>BIZ="biz"</code> */
    public static final String BIZ                   = "biz";

    /** Constant <code>PLUGIN="plugin"</code> */
    public static final String PLUGIN                = "plugin";

    /** Constant <code>CPU="cpu"</code> */
    public static final String CPU                   = "cpu";

    /** Constant <code>JVM="jvm"</code> */
    public static final String JVM                   = "jvm";

    /** Constant <code>MASTER_BIZ_HEALTH="masterBizHealth"</code> */
    public static final String MASTER_BIZ_HEALTH     = "masterBizHealth";

    /** Constant <code>MASTER_BIZ_INFO="masterBizInfo"</code> */
    public static final String MASTER_BIZ_INFO       = "masterBizInfo";

    /** Constant <code>NETWORK_INFO="networkInfo"</code> */
    public static final String NETWORK_INFO          = "networkInfo";

    /** Constant <code>HEALTH_ERROR="error"</code> */
    public static final String HEALTH_ERROR          = "error";

    /** Constant <code>HEALTH_ENDPOINT_ERROR="endpointError"</code> */
    public static final String HEALTH_ENDPOINT_ERROR = "endpointError";

    /** Constant <code>BIZ_INFO="bizInfo"</code> */
    public static final String BIZ_INFO              = "bizInfo";

    /** Constant <code>BIZ_LIST_INFO="bizListInfo"</code> */
    public static final String BIZ_LIST_INFO         = "bizListInfo";

    /** Constant <code>PLUGIN_INFO="pluginInfo"</code> */
    public static final String PLUGIN_INFO           = "pluginInfo";

    /** Constant <code>PLUGIN_LIST_INFO="pluginListInfo"</code> */
    public static final String PLUGIN_LIST_INFO      = "pluginListInfo";

    /** Constant <code>READINESS_HEALTHY="ACCEPTING_TRAFFIC"</code> */
    public static final String READINESS_HEALTHY     = "ACCEPTING_TRAFFIC";

    /** Constant <code>LOCAL_IP="localIP"</code> */
    public static final String LOCAL_IP              = "localIP";

    /** Constant <code>LOCAL_HOST_NAME="localHostName"</code> */
    public static final String LOCAL_HOST_NAME       = "localHostName";

    /**
     * <p>typeOfQuery.</p>
     *
     * @param type a {@link java.lang.String} object
     * @return a boolean
     */
    public static boolean typeOfQuery(String type) {
        return SYSTEM.equals(type) || BIZ.equals(type) || PLUGIN.equals(type);
    }

    /**
     * <p>typeOfInfo.</p>
     *
     * @param type a {@link java.lang.String} object
     * @return a boolean
     */
    public static boolean typeOfInfo(String type) {
        return Constants.BIZ.equals(type) || Constants.PLUGIN.equals(type);
    }
}
