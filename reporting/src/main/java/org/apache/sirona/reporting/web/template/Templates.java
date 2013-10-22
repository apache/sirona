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
package org.apache.sirona.reporting.web.template;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.reporting.web.plugin.PluginRepository;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.JdkLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

public final class Templates {
    public static final String RESOURCE_LOADER_KEY = "monitoring." + RuntimeConstants.RESOURCE_LOADER + ".class";

    private static String mapping;

    public static void init(final String context, final String filterMapping) {
        final Properties velocityConfiguration = new Properties();
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, JdkLogChute.class.getName());
        velocityConfiguration.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE.toString());
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE, Boolean.TRUE.toString());
        velocityConfiguration.setProperty(RuntimeConstants.RESOURCE_LOADER, "monitoring");
        velocityConfiguration.setProperty(RuntimeConstants.VM_LIBRARY, "/templates/macro.vm");
        velocityConfiguration.setProperty(RESOURCE_LOADER_KEY, Configuration.getProperty(Configuration.COMMONS_MONITORING_PREFIX + "reporting.resource-loader", ClasspathResourceLoader.class.getName()));
        Velocity.init(velocityConfiguration);

        if (filterMapping.isEmpty()) {
            mapping = context;
        } else {
            mapping = context + filterMapping;
        }
    }

    public static void htmlRender(final PrintWriter writer, final String template, final Map<String, ?> variables) {
        final VelocityContext context = newVelocityContext(variables);
        context.put("mapping", mapping);
        context.put("currentTemplate", template);
        context.put("plugins", PluginRepository.PLUGIN_INFO);
        if (context.get("templateId") == null) {
            context.put("templateId", template.replace(".vm", ""));
        }

        final Template velocityTemplate = Velocity.getTemplate("/templates/page.vm", "UTF-8");
        velocityTemplate.merge(context, writer);
    }

    public static void render(final PrintWriter writer, final String template, final Map<String, ?> variables) {
        final VelocityContext context = newVelocityContext(variables);
        context.put("mapping", mapping);
        final Template velocityTemplate = Velocity.getTemplate(template, "UTF-8");
        velocityTemplate.merge(context, writer);
    }

    private static VelocityContext newVelocityContext(final Map<String, ?> variables) {
        final VelocityContext context;
        if (variables.isEmpty()) {
            context = new VelocityContext();
        } else {
            context = new VelocityContext(variables);
        }
        return context;
    }

    private Templates() {
        // no-op
    }
}
