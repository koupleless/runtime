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
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.ark.spi.model.BizInfo.BizStateRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>SimpleBizInfo class.</p>
 *
 * @author dongnan
 * @since 2024/10/10
 * @version 1.0.0
 */
public class SimpleBizInfo {
    /**
     * Getter method for property <tt>state</tt>.
     *
     * @return property value of state
     */
    public String getS() {
        return s;
    }

    /**
     * Setter method for property <tt>state</tt>.
     *
     * @param s value to be assigned to property state
     */
    public void setS(BizState s) {
        switch (s) {
            case UNRESOLVED:
                this.s = "1";
                break;
            case RESOLVED:
                this.s = "2";
                break;
            case ACTIVATED:
                this.s = "3";
                break;
            case DEACTIVATED:
                this.s = "4";
                break;
            case BROKEN:
                this.s = "5";
                break;
        }
    }

    private String s;

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getN() {
        return n;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param n value to be assigned to property name
     */
    public void setN(String n) {
        this.n = n;
    }

    private String         n;

    private String         v;

    private BizStateRecord lrd;

    public static List<String> constructFromBizInfo(BizInfo info) {
        SimpleBizInfo simpleBizInfo = new SimpleBizInfo();
        simpleBizInfo.setS(info.getBizState());
        simpleBizInfo.setN(info.getBizName());
        simpleBizInfo.setV(info.getBizVersion());

        List<BizStateRecord> bizStateRecords = info.getBizStateRecords();

        if (!bizStateRecords.isEmpty()) {
            simpleBizInfo.setLrd(bizStateRecords.get(bizStateRecords.size() - 1));
        }

        ArrayList<String> ret = new ArrayList<>();
        ret.add(simpleBizInfo.getN());
        ret.add(simpleBizInfo.getV());
        ret.add(simpleBizInfo.getS());
        return ret;
    }

    /**
     * Getter method for property <tt>latestRecord</tt>.
     *
     * @return property value of latestRecord
     */
    public BizStateRecord getLrd() {
        return lrd;
    }

    /**
     * Setter method for property <tt>latestRecord</tt>.
     *
     * @param lrd value to be assigned to property latestRecord
     */
    public void setLrd(BizStateRecord lrd) {
        this.lrd = lrd;
    }

    /**
     * Getter method for property <tt>v</tt>.
     *
     * @return property value of v
     */
    public String getV() {
        return v;
    }

    /**
     * Setter method for property <tt>v</tt>.
     *
     * @param v value to be assigned to property v
     */
    public void setV(String v) {
        this.v = v;
    }
}
