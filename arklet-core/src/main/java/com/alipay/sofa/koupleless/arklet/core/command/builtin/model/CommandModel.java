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
package com.alipay.sofa.koupleless.arklet.core.command.builtin.model;

/**
 * <p>CommandModel class.</p>
 *
 * @author mingmen
 * @date 2023/6/14
 * @version 1.0.0
 */
public class CommandModel {
    private String id;
    private String desc;

    /**
     * <p>Constructor for CommandModel.</p>
     *
     * @param id a {@link java.lang.String} object
     * @param desc a {@link java.lang.String} object
     */
    public CommandModel(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getId() {
        return id;
    }

    /**
     * <p>Getter for the field <code>desc</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getDesc() {
        return desc;
    }

}
