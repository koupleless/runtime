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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizOps;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.util.ResourceUtils;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.net.URL;
import java.util.Map;

import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_INSTALL_ONLY_STRATEGY;

/**
 * <p>InstallBizHandler class.</p>
 *
 * @author mingmen
 * @since 2023/6/8
 * @version 1.0.0
 */
public class InstallBizHandler extends
                               AbstractCommandHandler<InstallBizHandler.Input, InstallBizHandler.InstallBizClientResponse>
                               implements ArkBizOps {
    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    /** {@inheritDoc} */
    @Override
    public Output<InstallBizClientResponse> handle(Input input) {
        MemoryPoolMXBean metaSpaceMXBean = ResourceUtils.getMetaSpaceMXBean();
        long startSpace = metaSpaceMXBean.getUsage().getUsed();
        try {
            InstallBizClientResponse installBizClientResponse = convertClientResponse(
                getOperationService().install(convertInstallRequest(input)));
            installBizClientResponse
                .setElapsedSpace(metaSpaceMXBean.getUsage().getUsed() - startSpace);
            if (ResponseCode.SUCCESS.equals(installBizClientResponse.getCode())) {
                return Output.ofSuccess(installBizClientResponse);
            } else {
                return Output.ofFailed(installBizClientResponse, "install biz not success!");
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    private InstallRequest convertInstallRequest(Input input) {
        return InstallRequest.builder().bizName(input.getBizName())
            .bizVersion(input.getBizVersion()).bizUrl(input.getBizUrl()).args(input.getArgs())
            .envs(input.getEnvs()).installStrategy(input.getInstallStrategy()).build();
    }

    private InstallBizClientResponse convertClientResponse(ClientResponse res) {
        InstallBizClientResponse installBizClientResponse = new InstallBizClientResponse();
        installBizClientResponse.setBizInfos(res.getBizInfos());
        installBizClientResponse.setCode(res.getCode());
        installBizClientResponse.setMessage(res.getMessage());
        return installBizClientResponse;
    }

    /** {@inheritDoc} */
    @Override
    public Command command() {
        return BuiltinCommand.INSTALL_BIZ;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Input input) throws CommandValidationException {
        InstallBizHandler.validateInput(input);
    }

    public static void validateInput(Input input) throws CommandValidationException {
        isTrue(!input.isAsync() || !StringUtils.isEmpty(input.getRequestId()),
            "requestId should not be blank when async is true");
        notBlank(input.getBizUrl(), "bizUrl should not be blank");

        if (StringUtils.isEmpty(input.getBizName()) || StringUtils.isEmpty(input.getBizVersion())) {
            LOGGER.warn(
                "biz name and version should not be empty, or it will reduce the performance.");
        }

        if (StringUtils.isEmpty(input.getBizName()) && StringUtils.isEmpty(input.getBizVersion())) {
            // if bizName and bizVersion is blank, it means that we should parse them from the jar. this will cost io operation.
            try {
                refreshBizInfoFromJar(input);
            } catch (IOException e) {
                throw new CommandValidationException(
                    String.format("refresh biz info from jar failed: %s", e.getMessage()));
            }
        } else if (!StringUtils.isEmpty(input.getBizName())
                   && !StringUtils.isEmpty(input.getBizVersion())) {
            // if bizName and bizVersion is not blank, it means that we should install the biz with the given bizName and bizVersion.
            // do nothing.
        } else {
            // if bizName or bizVersion is blank, it is invalid, throw exception.
            throw new CommandValidationException(
                "bizName and bizVersion should be both blank or both not blank.");
        }
    }

    private static void refreshBizInfoFromJar(Input input) throws IOException {
        // 如果入参里没有jar，例如模块卸载，这里就直接返回
        if (StringUtils.isEmpty(input.getBizUrl())) {
            return;
        }
        BizFactoryService bizFactoryService = ArkClient.getBizFactoryService();
        URL url = new URL(input.getBizUrl());
        File bizFile = ArkClient.createBizSaveFile(input.getBizName(), input.getBizVersion());
        FileUtils.copyInputStreamToFile(url.openStream(), bizFile);
        Biz biz = bizFactoryService.createBiz(bizFile);
        input.setBizName(biz.getBizName());
        input.setBizVersion(biz.getBizVersion());
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String              bizUrl;
        /**
         * can set --key=value, or just args
         */
        private String[]            args;

        /**
         * only used in multi-tenant jdk which support to set env for each Biz
         * Don't use this in non-multi-tenant jdk.
         */
        private Map<String, String> envs;
        /**
         * install biz strategy
         * default value is installOnly
         */
        private String              installStrategy = STRATEGY_INSTALL_ONLY_STRATEGY;
    }

    @Getter
    @Setter
    public static class InstallBizClientResponse extends ClientResponse {
        private long elapsedSpace;
    }

}
