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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>ClassUtils class.</p>
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ClassUtils.java, v 0.1 2024年08月05日 11:56 立蓬 Exp $
 * @since 1.3.1
 */
public class ClassUtils {
    /**
     * <p>getSuperClasses.</p>
     *
     * @param calzz a {@link java.lang.Class} object
     * @return a {@link java.util.List} object
     */
    public static List<Class<?>> getSuperClasses(Class<?> calzz) {
        List<Class<?>> listSuperClass = new ArrayList<Class<?>>();
        Class<?> superclass = calzz.getSuperclass();
        while (superclass != null) {
            if (superclass.getName().equals("java.lang.Object")) {
                break;
            }
            listSuperClass.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return listSuperClass;
    }
}
