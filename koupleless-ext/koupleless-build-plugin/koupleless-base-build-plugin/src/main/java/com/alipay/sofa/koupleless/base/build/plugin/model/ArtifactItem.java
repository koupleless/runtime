/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.base.build.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArtifactItem.java, v 0.1 2024年07月17日 18:10 立蓬 Exp $
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArtifactItem {
    private static final String GAV_SPLIT    = ":";

    private String              groupId;

    private String              artifactId;

    private String              version;

    private String              classifier;

    @Builder.Default
    private String              type         = "jar";

    @Builder.Default
    private String              scope        = "compile";

    @Override
    public int hashCode() {
        return Objects
                .hash(this.groupId, this.artifactId, this.type, this.version, this.classifier);
    }
}