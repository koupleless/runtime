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
package com.alipay.sofa.koupleless.ext.web.gateway;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.CompositeBizForwardsHandler;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.ForwardItem;
import com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.Forwards;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * <p>ForwardController class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
@Controller
@RequestMapping
public class ForwardController {
    private static final ArkletLogger  LOGGER            = ArkletLoggerFactory
        .getLogger(ForwardController.class);

    @Autowired
    private Forwards                   baseForwards;

    private Map<ClassLoader, Forwards> bizForwards       = CompositeBizForwardsHandler
        .getBizForwards();

    private static final String        SEPARATOR         = "/";
    private static final String        DOUBLE_SEPARATORS = SEPARATOR + SEPARATOR;

    /**
     * <p>redirect.</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object
     * @param response a {@link javax.servlet.http.HttpServletResponse} object
     * @throws javax.servlet.ServletException if any.
     * @throws java.io.IOException if any.
     */
    /**
     * <p>redirect.</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object
     * @param response a {@link javax.servlet.http.HttpServletResponse} object
     * @throws javax.servlet.ServletException if any.
     * @throws java.io.IOException if any.
     */
    @RequestMapping("/**")
    public void redirect(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        //定位forward信息
        URI uri = URI.create(request.getRequestURL().toString());
        String host = uri.getHost();
        String sourcePath = uri.getPath();
        if (!StringUtils.hasLength(sourcePath)) {
            sourcePath = Forwards.ROOT_PATH;
        }
        ForwardItem forwardItem = getForwardItem(host, sourcePath);
        if (forwardItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        //计算要跳转的路径
        String contextPath = forwardItem.getContextPath();
        String targetPath = forwardItem.getTo()
                            + sourcePath.substring(forwardItem.getFrom().length());
        if (targetPath.startsWith(DOUBLE_SEPARATORS)) {
            targetPath = targetPath.substring(1);
        }

        LOGGER.info("uri with host {}, sourcePath {} will forward to {}", host, sourcePath,
            contextPath + targetPath);

        ServletContext currentContext = request.getServletContext();
        ServletContext nextContext = currentContext.getContext(contextPath + targetPath);
        if (currentContext == nextContext) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        RequestDispatcher dispatcher = nextContext.getRequestDispatcher(targetPath);
        dispatcher.forward(request, response);
    }

    /**
     * Matching Rule: Preferentially apply the forward configuration of the biz, and if not available, match the forward configuration of base.
     * @param host the host of uri
     * @param sourcePath the path of uri
     * @return com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway.ForwardItem
     */
    private ForwardItem getForwardItem(String host, String sourcePath) {
        ForwardItem item;

        // match biz forward first
        for (Map.Entry<ClassLoader, Forwards> entry : bizForwards.entrySet()) {
            item = entry.getValue().getForwardItem(host, sourcePath);
            if (null != item) {
                Biz biz = ArkClient.getBizManagerService().getBizByClassLoader(entry.getKey());
                LOGGER.info("biz {} forward configuration matches: {} {}", biz.getIdentity(), host,
                    sourcePath);
                return item;
            }
        }

        // match base forward
        item = baseForwards.getForwardItem(host, sourcePath);

        if (null != item) {
            LOGGER.info("base forward configuration matches: {} {}", host, sourcePath);
        }

        return item;
    }
}
