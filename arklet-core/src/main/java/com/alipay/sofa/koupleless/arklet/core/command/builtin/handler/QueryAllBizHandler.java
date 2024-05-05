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
package com.alipay.sofa.koupleless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>QueryAllBizHandler class.</p>
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
public class QueryAllBizHandler extends AbstractCommandHandler<InputMeta, List<BizInfo>> {

    private static final String BASE_ARK_MAIN_CLASS = "embed main";

    /** {@inheritDoc} */
    @Override
    public Output<List<BizInfo>> handle(InputMeta inputMeta) {
        List<Biz> bizList = getOperationService().queryBizList();
        List<BizInfo> bizInfos = new ArrayList<>(bizList.size());
        for (Biz biz : bizList) {
            if (Objects.equals(biz.getMainClass(), BASE_ARK_MAIN_CLASS)) {
                continue;
            }
            BizInfo model = new BizInfo();
            model.setBizName(biz.getBizName());
            model.setBizVersion(biz.getBizVersion());
            model.setBizState(biz.getBizState());
            model.setMainClass(biz.getMainClass());
            model.setWebContextPath(biz.getWebContextPath());
            model.setClassLoader(biz.getBizClassLoader());
            model.setBizStateRecords(biz.getBizStateRecords());
            bizInfos.add(model);
        }
        return Output.ofSuccess(bizInfos);
    }

    /** {@inheritDoc} */
    @Override
    public Command command() {
        return BuiltinCommand.QUERY_ALL_BIZ;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(InputMeta input) throws CommandValidationException {
        // no need
    }
}
