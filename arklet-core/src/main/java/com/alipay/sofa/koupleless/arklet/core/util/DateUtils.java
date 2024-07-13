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
package com.alipay.sofa.koupleless.arklet.core.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: DateUtils.java, v 0.1 2024年07月02日 14:13 立蓬 Exp $
 */
public class DateUtils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        sdf.setTimeZone(TimeZone.getDefault());
    }

    public static String format(long time) {
        return sdf.format(time);
    }

    public static String getCurrentTime() {
        return sdf.format(System.currentTimeMillis());
    }
}