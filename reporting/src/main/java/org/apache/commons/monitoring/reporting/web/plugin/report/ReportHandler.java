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

import org.apache.commons.monitoring.reporting.web.handler.Handler;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;
import org.apache.commons.monitoring.reporting.web.plugin.report.format.Format;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportHandler implements Handler {
    private final FormatReportHandler html;
    private final FormatReportHandler csv;
    private final FormatReportHandler json;
    private final FormatReportHandler xml;
    private final ClearHandler clear;

    public ReportHandler() {
        html = new FormatReportHandler(Format.Defaults.HTML);
        csv = new FormatReportHandler(Format.Defaults.CSV);
        json = new FormatReportHandler(Format.Defaults.JSON);
        xml = new FormatReportHandler(Format.Defaults.XML);
        clear = new ClearHandler();
    }

    @Override
    public Renderer handle(final HttpServletRequest request, final HttpServletResponse response, final String path) {
        if (path.endsWith(".csv")) {
            return csv.handle(request, response, path);
        }
        if (path.endsWith(".json")) {
            return json.handle(request, response, path);
        }
        if (path.endsWith(".xml")) {
            return xml.handle(request, response, path);
        }
        if (path.endsWith("/clear")) {
            return clear.handle(request, response, path);
        }

        return html.handle(request, response, path);
    }
}
