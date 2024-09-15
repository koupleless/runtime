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
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.spi.model.Biz;
import org.springframework.context.ApplicationContext;

import static com.alipay.sofa.koupleless.common.util.BizRuntimeContextUtils.getApplicationContext;

/**
 * <p>SpringUtils class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: SpringUtils.java, v 0.1 2024年07月04日 14:31 立蓬 Exp $
 */
public class SpringUtils {
    /**
     * <p>containsBean.</p>
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     * @param beanName a {@link java.lang.String} object
     * @return a boolean
     */
    public static boolean containsBean(Biz biz, String beanName) {
        ApplicationContext bizContext = getApplicationContext(biz);
        return null != bizContext && bizContext.containsBean(beanName);
    }

    /**
     * <p>getBean.</p>
     *
     * @param biz a {@link com.alipay.sofa.ark.spi.model.Biz} object
     * @param beanName a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     */
    public static Object getBean(Biz biz, String beanName) {
        if (containsBean(biz, beanName)) {
            return getApplicationContext(biz).getBean(beanName);
        }
        return null;
    }
}
