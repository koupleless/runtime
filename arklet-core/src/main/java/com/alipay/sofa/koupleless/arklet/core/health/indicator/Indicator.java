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
package com.alipay.sofa.koupleless.arklet.core.health.indicator;

import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health.HealthBuilder;

import java.util.Map;

/**
 * <p>Abstract Indicator class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
public abstract class Indicator {

    private final String indicatorId;

    /**
     * <p>Constructor for Indicator.</p>
     *
     * @param indicatorId a {@link java.lang.String} object
     */
    public Indicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    /**
     * get health details
     *
     * @return a map of health details
     */
    protected abstract Map<String, Object> getHealthDetails();

    /**
     * get indicator id
     *
     * @return indicator id
     */
    public String getIndicatorId() {
        return indicatorId;
    }

    /**
     * get health model
     *
     * @param builder input health builder
     * @return health model
     */
    public Health getHealthModel(HealthBuilder builder) {
        return builder.init().putHealthData(getIndicatorId(), getHealthDetails()).build();
    }
}
