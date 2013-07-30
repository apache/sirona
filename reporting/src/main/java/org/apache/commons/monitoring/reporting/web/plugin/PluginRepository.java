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

import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.reporting.web.handler.Handler;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

public class PluginRepository {
    public static Collection<PluginInfo> PLUGIN_INFO = new CopyOnWriteArrayList<PluginInfo>();

    static {
        for (final Plugin plugin : ServiceLoader.load(Plugin.class, Plugin.class.getClassLoader())) {
            final String name = plugin.name();
            if (name == null) {
                throw new IllegalArgumentException("plugin name can't be null");
            }
            if (!Configuration.is(name + ".activated", true)) {
                continue;
            }

            final String[] mappings = plugin.mappings();
            final Class<? extends Handler> handler = plugin.handler();
            if (mappings != null && handler != null) {
                try {
                    final Handler handlerInstance = new PluginDecoratorHandler(handler.newInstance(), name);
                    for (final String mapping : mappings) {
                        PLUGIN_INFO.add(new PluginInfo(mapping, handlerInstance, name));
                    }
                } catch (final Exception e) {
                    throw new MonitoringException(e);
                }
            }
        }
    }

    public static class PluginInfo {
        private final String url;
        private final Handler handler;
        private final String name;

        public PluginInfo(final String url, final Handler handler, final String name) {
            this.url = url;
            this.handler = handler;
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public Handler getHandler() {
            return handler;
        }

        public String getName() {
            return name;
        }
    }
}
