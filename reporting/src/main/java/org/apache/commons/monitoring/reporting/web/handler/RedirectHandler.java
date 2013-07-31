/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.monitoring.reporting.web.handler;

import org.apache.commons.monitoring.MonitoringException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RedirectHandler implements Handler {
    @Override
    public Renderer handle(final HttpServletRequest request, final HttpServletResponse response, final String path) {
        preRedirect();
        try {
            response.sendRedirect(request.getRequestURI().substring(0, request.getRequestURI().length() - from().length()) + to());
        } catch (final Exception e) {
            throw new MonitoringException(e);
        }
        return null;
    }

    protected void preRedirect() {
        // no-op
    }

    public abstract String from();
    public abstract String to();
}
