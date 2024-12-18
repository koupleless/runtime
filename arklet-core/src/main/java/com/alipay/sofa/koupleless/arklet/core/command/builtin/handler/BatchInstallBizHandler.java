/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.BatchInstallBizHandler.BatchInstallInput;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.BatchInstallBizHandler.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizOps;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import com.alipay.sofa.koupleless.arklet.core.util.ResourceUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;

import static com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand.BATCH_INSTALL_BIZ;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BatchInstallBizHandler.java, v 0.1 2024年12月18日 15:04 立蓬 Exp $
 */
public class BatchInstallBizHandler extends AbstractCommandHandler<BatchInstallInput, BatchInstallResponse> implements ArkBizOps {

    @Override
    public void validate(BatchInstallInput batchInstallInput) throws CommandValidationException {
        notNull(batchInstallInput.getBizList(), "bizList is null");

        for(Input bizInput: batchInstallInput.bizList){
            bizInput.setAsync(false);
            InstallBizHandler.validateInput(bizInput);
        }
    }

    @Override
    public Output<BatchInstallResponse> handle(BatchInstallInput batchInstallInput) {
        MemoryPoolMXBean metaSpaceMXBean = ResourceUtils.getMetaSpaceMXBean();
        long startSpace = metaSpaceMXBean.getUsage().getUsed();
        try {
            BatchInstallResponse response = convertClientResponse(
                    getOperationService().batchInstall(convertBatchInstallRequest(batchInstallInput)));
            response
                    .setElapsedSpace(metaSpaceMXBean.getUsage().getUsed() - startSpace);
            if (ResponseCode.SUCCESS.equals(response.getCode())) {
                return Output.ofSuccess(response);
            } else {
                return Output.ofFailed(response, "install biz not success!");
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    @Override
    public Command command() {
        return BATCH_INSTALL_BIZ;
    }

    private BatchInstallRequest convertBatchInstallRequest(BatchInstallInput input) {
        ArrayList<InstallRequest> installRequestList = new ArrayList<>();

        for(Input bizInput: input.getBizList()){
            installRequestList.add(InstallRequest.builder()
                    .bizName(bizInput.getBizName())
                    .bizVersion(bizInput.getBizVersion())
                    .bizUrl(bizInput.getBizUrl())
                    .args(bizInput.getArgs())
                    .envs(bizInput.getEnvs())
                    .installStrategy(bizInput.getInstallStrategy())
                    .build());
        }
        return BatchInstallRequest.builder().installRequests(installRequestList.toArray(new InstallRequest[0])).build();
    }

    private BatchInstallResponse convertClientResponse(com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse res) {
        BatchInstallResponse response = new BatchInstallResponse();
        response.setBizUrlToResponse(res.getBizUrlToResponse());
        response.setCode(res.getCode());
        response.setMessage(res.getMessage());
        return response;
    }

    @Getter
    @Setter
    public static class BatchInstallInput extends InputMeta {
        private Input[] bizList;
    }

    @Getter
    @Setter
    public static class BatchInstallResponse extends com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse {
        private long elapsedSpace;
    }
}