/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sirona.agent.webapp.pull.registration;

import org.apache.sirona.agent.webapp.pull.servlet.PullServlet;
import org.apache.sirona.configuration.Configuration;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Set;

public class SironaPullAgentRegistration implements ServletContainerInitializer {
    private static final String DEFAULT_MAPPING = "/sirona/pull";

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        final ServletRegistration.Dynamic dynamic = ctx.addServlet("Sirona Pull Agent", PullServlet.class.getName());
        dynamic.setLoadOnStartup(1);
        dynamic.addMapping(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "agent.pull.mapping", DEFAULT_MAPPING));
    }
}
