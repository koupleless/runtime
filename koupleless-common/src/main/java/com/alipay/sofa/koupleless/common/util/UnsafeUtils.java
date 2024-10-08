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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Provides utilities for accessing {@link Unsafe}.
 *
 * @author oxsean@gmail.com
 * @version 1.0.0
 */
public final class UnsafeUtils {

    public static final Unsafe UNSAFE;

    static {
        Unsafe unsafe = null;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Throwable ignored) {
        }
        UNSAFE = unsafe;
    }

    private UnsafeUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T readStaticField(Class<?> cls, Field field) {
        Objects.requireNonNull(UNSAFE, "Unsafe initialization failed");
        return (T) UNSAFE.getObject(cls, UNSAFE.staticFieldOffset(field));
    }

    public static void writeStaticField(Class<?> cls, Field field, Object value) {
        Objects.requireNonNull(UNSAFE, "Unsafe initialization failed");
        UNSAFE.putObject(cls, UNSAFE.staticFieldOffset(field), value);
    }
}
