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
package com.alipay.sofa.koupleless.test.suite.spring.common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CodeNoobKing
 * @date 2024/3/29
 **/
public class SpringUtils {

    public static List<String> getBasePackages(Class<?> mainClass) {
        ComponentScan componentScan = mainClass.getAnnotation(ComponentScan.class);
        if (componentScan != null) {
            return Arrays.asList(componentScan.basePackages());
        }

        ComponentScans componentScans = mainClass.getAnnotation(ComponentScans.class);
        if (componentScans != null) {
            return Arrays.stream(componentScans.value()).map(ComponentScan::basePackages)
                .flatMap(Arrays::stream).collect(Collectors.toList());
        }

        SpringBootApplication springBootApplication = mainClass
            .getAnnotation(SpringBootApplication.class);
        if (springBootApplication != null && springBootApplication.scanBasePackages() != null) {
            return Arrays.asList(springBootApplication.scanBasePackages());
        }

        if (springBootApplication != null) {
            return Arrays.asList(mainClass.getPackage().getName());
        }

        throw new RuntimeException("No basePackages found in mainClass: " + mainClass.getName());
    }
}
