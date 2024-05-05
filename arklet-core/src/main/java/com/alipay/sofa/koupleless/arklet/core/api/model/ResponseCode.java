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
package com.alipay.sofa.koupleless.arklet.core.api.model;

/**
 * <p>ResponseCode class.</p>
 *
 * @author mingmen
 * @since 2023/6/26
 * @version 1.0.0
 */
public enum ResponseCode {
                          SUCCESS(200), FAILED(400), CMD_NOT_FOUND(404), CMD_PROCESS_INTERNAL_ERROR(500);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    /**
     * <p>Getter for the field <code>code</code>.</p>
     *
     * @return a int
     */
    public int getCode() {
        return code;
    }
}
