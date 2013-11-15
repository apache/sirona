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
package org.apache.sirona.reporting.web.handler;

import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.template.Templates;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

public class TemplateHelper {
    private static final String EMPTY_STRING = "";

    private final PrintWriter writer;
    private final Map<String, ?> params;


    public TemplateHelper(final PrintWriter writer, final Map<String, ?> params) {
        this.writer = writer;
        this.params = params;
    }

    public void renderHtml(final String template) {
        renderHtml(template, Collections.<String, Object>emptyMap());
    }

    public void renderHtml(final String template, final Map<String, ? extends Object> userParams) {
        Templates.htmlRender(writer, template, new MapBuilder<String, Object>().set(Map.class.cast(params)).set(Map.class.cast(userParams)).build());
    }

    public void renderPlain(final String template, final Map<String, ?> params) {
        Templates.render(writer, template, params);
    }

    public void renderPlain(final String template) {
        renderPlain(template, Collections.<String, Object>emptyMap());
    }

    public void write(final String message) {
        writer.write(message);
    }

    public static String nullProtection(final String value) {
        if (value == null) {
            return EMPTY_STRING;
        }
        return value;
    }
}
