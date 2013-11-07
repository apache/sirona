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
package org.apache.sirona.agent.webapp.pull.servlet;

import org.apache.sirona.agent.webapp.pull.repository.PullRepository;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.repositories.Repository;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PullServlet extends HttpServlet {
    private static final String APPLICATION_JSON = "application/json";

    private PullRepository repository;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        repository = PullRepository.class.cast(Repository.INSTANCE);

        final String registration = config.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "pull.url");
        if (registration != null) { // needs to have configured org.apache.sirona.cube.CubeBuilder in sirona.properties
            repository.register(registration);
        } // else collector should be aware or it with another way -> config in the collector
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(APPLICATION_JSON);
        resp.getWriter().write(repository.snapshot());
    }
}
