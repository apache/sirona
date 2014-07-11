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

import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.template.Templates;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilteringEndpoints {
    private static final String BOOTSTRAP_CSS = "/resources/css/bootstrap.css";
    private static final String SIRONA_CSS = "/resources/css/sirona.css";
    private static final String RESOURCES = "/resources/.*";

    private ResourceLoader rl;

    public FilteringEndpoints() {
        try {
            rl = ResourceLoader.class.cast(FilteringEndpoints.class.getClassLoader().loadClass((String) Templates.property(Templates.RESOURCE_LOADER_KEY)).newInstance());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Regex(SIRONA_CSS)
    public void filterCss(final TemplateHelper helper) {
        helper.renderPlain(SIRONA_CSS);
    }

    @Regex(BOOTSTRAP_CSS)
    public void filterBootstrapCss(final TemplateHelper helper) {
        helper.renderPlain(BOOTSTRAP_CSS);
    }

    @Regex(RESOURCES)
    public void filterOtherResources(final HttpServletRequest req) {
        final String requestURI = req.getRequestURI();

        final InputStream is;
        try {
            is = rl.getResourceStream(requestURI.substring(((String) req.getAttribute("baseUri")).length()));
        } catch (final ResourceNotFoundException rnfe) {
            return;
        }

        try {
            byte[] buffer = new byte[1024];
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            req.setAttribute("resourceCache", os);
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        } finally {
            try {
                is.close();
            } catch (final IOException e) {
                // no-op
            }
        }
    }
}
