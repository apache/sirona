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
package org.apache.commons.monitoring.reporting.web.plugin;

import org.apache.commons.monitoring.reporting.web.handler.Handler;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PluginDecoratorHandler implements Handler {
    private final Handler delegate;
    private final String plugin;

    public PluginDecoratorHandler(final Handler handler, final String name) {
        delegate = handler;
        plugin = name;
    }

    @Override
    public Renderer handle(final HttpServletRequest request, final HttpServletResponse response) {
        return new PluginDecoratorRenderer(delegate.handle(request, response), plugin);
    }

    private static class PluginDecoratorRenderer implements Renderer {
        private final Renderer delegate;
        private final String plugin;

        public PluginDecoratorRenderer(final Renderer handle, final String name) {
            delegate = handle;
            plugin = name;
        }

        @Override
        public void render(final PrintWriter writer, final Map<String, ?> params) {
            final Map<String, Object> map = new HashMap<String, Object>();
            if (params != null && !params.isEmpty()) {
                map.putAll(params);
            }
            map.put("templateId", plugin);

            delegate.render(writer, map);
        }
    }
}
