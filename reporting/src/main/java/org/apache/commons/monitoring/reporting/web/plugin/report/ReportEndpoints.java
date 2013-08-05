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
package org.apache.commons.monitoring.reporting.web.plugin.report;

import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.reporting.web.handler.api.Regex;
import org.apache.commons.monitoring.reporting.web.handler.api.Template;
import org.apache.commons.monitoring.reporting.web.plugin.report.format.Format;
import org.apache.commons.monitoring.repositories.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ReportEndpoints {
    @Regex
    public Template html(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.HTML);
    }

    @Regex(".csv")
    public Template csv(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.CSV);
    }

    @Regex(".json")
    public Template json(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.JSON);
    }

    @Regex(".xml")
    public Template xml(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.XML);
    }

    @Regex("/clear")
    public void clear(final HttpServletRequest request, final HttpServletResponse response) {
        Repository.INSTANCE.clear();
        try {
            response.sendRedirect(request.getRequestURI().substring(0, request.getRequestURI().length() - "/clear".length()));
        } catch (final IOException e) {
            throw new MonitoringException(e);
        }
    }

    private Template renderFormat(final HttpServletRequest request, final HttpServletResponse response, final Format format) {
        response.setContentType(format.type());
        return format.render(request.getParameterMap());
    }
}
