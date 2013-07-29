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

import org.apache.commons.monitoring.reporting.format.Format;
import org.apache.commons.monitoring.reporting.web.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportHandler implements Handler {
    private static Map<String, Format> extensions = new HashMap<String, Format>();
    private static Map<String, Format> formats = new HashMap<String, Format>();

    static {
        formats.put("application/json", Format.Defaults.JSON);
        formats.put("text/javascript", Format.Defaults.JSON);
        formats.put("application/xml", Format.Defaults.XML);
        formats.put("text/xml", Format.Defaults.XML);
        formats.put("text/plain", Format.Defaults.CSV);
        formats.put("text/csv", Format.Defaults.CSV);
        formats.put("text/html", Format.Defaults.HTML);

        extensions.put("json", Format.Defaults.JSON);
        extensions.put("js", Format.Defaults.JSON);
        extensions.put("xml", Format.Defaults.XML);
        extensions.put("csv", Format.Defaults.CSV);
        extensions.put("html", Format.Defaults.HTML);
        extensions.put("htm", Format.Defaults.HTML);
        extensions.put("xhtml", Format.Defaults.HTML);
    }

    @Override
    public Renderer handle(final HttpServletRequest request, final HttpServletResponse response) {
        Format format = null;

        final String path = request.getRequestURI();
        final int dot = path.lastIndexOf('.');
        if (dot >= 0) {
            format = extensions.get(path.substring(dot + 1).toLowerCase(Locale.ENGLISH));
        } else {
            final String mime = HttpUtils.parseAccept(request.getHeader("Accept"));
            if (mime != null) {
                format = formats.get(mime);
            }
        }
        if (format == null) {
            format = Format.Defaults.CSV;
        }

        response.setContentType(format.type());
        return format;
    }
}
