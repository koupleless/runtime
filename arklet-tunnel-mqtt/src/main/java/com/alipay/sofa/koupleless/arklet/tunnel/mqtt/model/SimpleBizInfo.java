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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model;

import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.BizInfo.BizStateRecord;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * <p>SimpleBizInfo class.</p>
 *
 * @author dongnan
 * @since 2024/10/10
 * @version 1.0.0
 */
@Data
public class SimpleBizInfo implements Serializable {
    // 序列化版本号
    private static final long serialVersionUID = 1L;

    private String            state;

    private String            name;

    private String            version;

    /**
     * laster state record
     */
    private BizStateRecord    latestStateRecord;

    public static SimpleBizInfo constructFromBizInfo(BizInfo info) {
        SimpleBizInfo simpleBizInfo = new SimpleBizInfo();
        simpleBizInfo.setState(info.getBizState().getBizState());
        simpleBizInfo.setName(info.getBizName());
        simpleBizInfo.setVersion(info.getBizVersion());

        List<BizStateRecord> bizStateRecords = info.getBizStateRecords();

        if (bizStateRecords != null && !bizStateRecords.isEmpty()) {
            simpleBizInfo.setLatestStateRecord(bizStateRecords.get(bizStateRecords.size() - 1));
        }

        return simpleBizInfo;
    }
}
