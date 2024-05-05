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

import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>PluginHealthMeta class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
public class PluginHealthMeta {

    private String pluginName;

    private String groupId;

    private String artifactId;

    private String pluginVersion;

    private String pluginUrl;

    private String pluginActivator;

    /**
     * <p>Getter for the field <code>pluginName</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * <p>Getter for the field <code>groupId</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * <p>Getter for the field <code>artifactId</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * <p>Getter for the field <code>pluginVersion</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * <p>Getter for the field <code>pluginActivator</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getPluginActivator() {
        return pluginActivator;
    }

    /**
     * <p>Getter for the field <code>pluginUrl</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getPluginUrl() {
        return pluginUrl;
    }

    /**
     * <p>Setter for the field <code>pluginName</code>.</p>
     *
     * @param pluginName a {@link java.lang.String} object
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * <p>Setter for the field <code>groupId</code>.</p>
     *
     * @param groupId a {@link java.lang.String} object
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * <p>Setter for the field <code>artifactId</code>.</p>
     *
     * @param artifactId a {@link java.lang.String} object
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * <p>Setter for the field <code>pluginVersion</code>.</p>
     *
     * @param pluginVersion a {@link java.lang.String} object
     */
    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    /**
     * <p>Setter for the field <code>pluginUrl</code>.</p>
     *
     * @param pluginUrl a {@link java.lang.String} object
     */
    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    /**
     * <p>Setter for the field <code>pluginActivator</code>.</p>
     *
     * @param pluginActivator a {@link java.lang.String} object
     */
    public void setPluginActivator(String pluginActivator) {
        this.pluginActivator = pluginActivator;
    }

    /**
     * <p>createPluginMeta.</p>
     *
     * @param plugin a {@link com.alipay.sofa.ark.spi.model.Plugin} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.health.model.PluginHealthMeta} object
     */
    public static PluginHealthMeta createPluginMeta(Plugin plugin) {
        AssertUtils.assertNotNull(plugin, "can not find plugin");
        PluginHealthMeta pluginHealthMeta = PluginHealthMeta
            .createPluginMeta(plugin.getPluginName(), plugin.getVersion());
        pluginHealthMeta.setGroupId(plugin.getGroupId());
        pluginHealthMeta.setArtifactId(plugin.getArtifactId());
        pluginHealthMeta.setPluginActivator(plugin.getPluginActivator());
        pluginHealthMeta.setPluginUrl(plugin.getPluginURL().getPath());
        return pluginHealthMeta;
    }

    /**
     * <p>createPluginMeta.</p>
     *
     * @param pluginName a {@link java.lang.String} object
     * @param pluginVersion a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.arklet.core.health.model.PluginHealthMeta} object
     */
    public static PluginHealthMeta createPluginMeta(String pluginName, String pluginVersion) {
        PluginHealthMeta pluginHealthMeta = new PluginHealthMeta();
        pluginHealthMeta.setPluginName(pluginName);
        pluginHealthMeta.setPluginVersion(pluginVersion);
        return pluginHealthMeta;
    }

    /**
     * <p>createPluginMetaList.</p>
     *
     * @param pluginList a {@link java.util.List} object
     * @return a {@link java.util.List} object
     */
    public static List<PluginHealthMeta> createPluginMetaList(List<Plugin> pluginList) {
        List<PluginHealthMeta> pluginHealthMetaList = new ArrayList<>();
        for (Plugin plugin : pluginList) {
            pluginHealthMetaList.add(createPluginMeta(plugin));
        }
        return pluginHealthMetaList;
    }
}
