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

import org.apache.commons.monitoring.reporting.template.Templates;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

public class FilteringHandler implements Handler {
    public static final FilteringHandler INSTANCE = new FilteringHandler();

    private FilteringHandler() {
        // no-op
    }

    @Override
    public Renderer handle(final HttpServletRequest request, final HttpServletResponse response) {
        return new FilteringRenderer(request.getRequestURI().substring(request.getContextPath().length()));
    }

    private static class FilteringRenderer implements Renderer {
        private final String path;

        public FilteringRenderer(final String path) {
            this.path = path;
        }

        @Override
        public void render(final PrintWriter writer, final Map<String, ?> params) {
            Templates.render(writer, path, Collections.<String, Object>emptyMap());
        }
    }
}
