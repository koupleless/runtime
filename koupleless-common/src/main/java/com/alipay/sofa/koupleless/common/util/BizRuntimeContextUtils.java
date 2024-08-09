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
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.springframework.context.ApplicationContext;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizRuntimeUtils.java, v 0.1 2024年07月04日 14:28 立蓬 Exp $
 */
public class BizRuntimeContextUtils {
    public static ApplicationContext getApplicationContext(Biz biz) {
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getRuntimeMap()
            .get(biz.getBizClassLoader());
        if (null == bizRuntimeContext) {
            return null;
        }
        return bizRuntimeContext.getRootApplicationContext();
    }
}