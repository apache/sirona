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
package org.apache.sirona.reporting.web.plugin;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.reporting.web.handler.internal.EndpointInfo;
import org.apache.sirona.reporting.web.handler.internal.Invoker;
import org.apache.sirona.reporting.web.plugin.api.Local;
import org.apache.sirona.reporting.web.plugin.api.Plugin;
import org.apache.sirona.spi.SPI;
import org.apache.sirona.util.Environment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public final class PluginRepository {
    public static Collection<PluginInfo> PLUGIN_INFO = new CopyOnWriteArrayList<PluginInfo>();

    /** This flag is used to activate a plugin by its name [plugin.name]. The default value is true which means that
     * every plugin discovered will be used.
     */
    public static final String ACTIVATED_FLAG = ".activated";

    static {
        final boolean acceptLocal = !Environment.isCollector();
        for (final Plugin plugin : SPI.INSTANCE.find(Plugin.class, Plugin.class.getClassLoader())) {
            if (!acceptLocal && plugin.getClass().getAnnotation(Local.class) != null) {
                continue;
            }

            final String name = plugin.name();
            if (name == null) {
                throw new IllegalArgumentException("plugin name can't be null");
            }
            if (!Configuration.is(name + ACTIVATED_FLAG, true)) {
                continue;
            }

            final String mapping = plugin.mapping();
            final Class<?> handler = plugin.endpoints();
            if (mapping != null) {
                PLUGIN_INFO.add(new PluginInfo(mapping, name, EndpointInfo.build(handler, plugin.name(), plugin.mapping())));
            }
        }
    }

    private PluginRepository() {
        // no-op
    }

    public static class PluginInfo {
        private final String url;
        private final String name;
        private final EndpointInfo info;

        public PluginInfo(final String url, String name, final EndpointInfo info) {
            this.url = url;
            this.name = name;
            this.info = info;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public Map<Pattern, Invoker> getInvokers() {
            return info.getInvokers();
        }
    }
}
