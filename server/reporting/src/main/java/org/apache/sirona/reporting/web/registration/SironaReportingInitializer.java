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
package org.apache.sirona.reporting.web.registration;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.reporting.web.SironaController;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;
import java.util.Set;

public class SironaReportingInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        final String activated = ctx.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "reporting.activated");
        if ("false".equalsIgnoreCase(activated)) {
            return;
        }

        String mapping = ctx.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "reporting.mapping");
        if (mapping == null) {
            mapping = "/sirona";
        }
        if (mapping.endsWith("*")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        if (mapping.endsWith("/")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }

        final SironaController controller = new SironaController();
        controller.setMapping(mapping);
        ctx.addFilter("sirona-reporting", controller).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, mapping + "/*");
    }
}
