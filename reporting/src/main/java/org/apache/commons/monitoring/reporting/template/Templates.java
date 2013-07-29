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
package org.apache.commons.monitoring.reporting.template;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.JdkLogChute;

import javax.servlet.ServletContext;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

public final class Templates {
    private static String base;
    private static String mapping;
    private static ServletContext servletContext;

    public static void init(final ServletContext context, final String filterMapping) {
        servletContext = context;

        final Properties velocityConfiguration = new Properties();
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, JdkLogChute.class.getName());
        velocityConfiguration.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE.toString());
        velocityConfiguration.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE, Boolean.TRUE.toString());
        velocityConfiguration.setProperty(RuntimeConstants.RESOURCE_LOADER, "monitoring");
        velocityConfiguration.setProperty("monitoring." + RuntimeConstants.RESOURCE_LOADER + ".class", WebResourceLoader.class.getName());
        Velocity.init(velocityConfiguration);

        base = context.getContextPath();
        if (filterMapping.isEmpty()) {
            mapping = context.getContextPath();
        } else {
            mapping = context.getContextPath() + filterMapping;
        }
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void htmlRender(final PrintWriter writer, final String template, final Map<String, ?> variables) {
        final VelocityContext context = newVelocityContext(variables);
        context.put("base", base);
        context.put("mapping", mapping);
        context.put("currentTemplate", template);

        final Template velocityTemplate = Velocity.getTemplate("/templates/page.vm", "UTF-8");
        velocityTemplate.merge(context, writer);
    }

    public static void render(final PrintWriter writer, final String template, final Map<String, ?> variables) {
        final VelocityContext context = newVelocityContext(variables);
        context.put("base", base);
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
