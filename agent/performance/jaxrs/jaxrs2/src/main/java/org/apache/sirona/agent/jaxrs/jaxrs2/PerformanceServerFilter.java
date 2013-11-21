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
package org.apache.sirona.agent.jaxrs.jaxrs2;

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Provider
public class PerformanceServerFilter extends AbstractPerformanceInterceptor<ContainerRequestContext> implements ContainerRequestFilter, ContainerResponseFilter {
    public static final Role ROLE = new Role(Role.PERFORMANCES.getName() + "-jaxrs2", Role.PERFORMANCES.getUnit());

    private static final String SIRONA_PERFORMANCE_PROP = "sirona-performance";
    private static final String NO_METHOD = "X";

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        context.setProperty(SIRONA_PERFORMANCE_PROP, before(context, getCounterName(context)));
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        final Context context = Context.class.cast(requestContext.getProperty(SIRONA_PERFORMANCE_PROP));
        if (context != null) {
            context.stop(null);
        }
    }

    @Override
    protected String getCounterName(final ContainerRequestContext context) {
        final UriInfo uriInfo = context.getUriInfo();

        String base = uriInfo.getBaseUri().getPath();
        if (!base.endsWith("/")) {
            base += "/";
        }

        String method = context.getMethod();
        if (method == null) {
            method = NO_METHOD;
        }

        return computePath(method, base, uriInfo.getPathSegments(), uriInfo.getPathParameters());
    }

    // if JAX-RS 2 provides a better way to do it just change it please!
    private String computePath(final String method, final String base, final List<PathSegment> pathSegments, final MultivaluedMap<String, String> pathParameters) {
        final StringBuilder builder = new StringBuilder(method).append("-").append(base);
        final Collection<String> alreadyUsed = new HashSet<String>();
        for (final PathSegment segment : pathSegments) {
            String found = null;

            final String path = segment.getPath();

            for (final Map.Entry<String, List<String>> entry : pathParameters.entrySet()) {
                boolean newSegment = false;
                for (final String value : entry.getValue()) {
                    if (value != null && value.equals(path)) {
                        found = entry.getKey();
                        newSegment = !alreadyUsed.contains(found);
                        if (found != null) {
                            break;
                        }
                    }
                }
                if (newSegment) {
                    break;
                }
            }

            if (found == null) {
                builder.append(path);
            } else {
                builder.append("{").append(found).append("}");
                alreadyUsed.add(found);
            }
            builder.append("/");
        }
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    protected Role getRole() {
        return ROLE;
    }

    @Override
    protected Object proceed(final ContainerRequestContext context) throws Throwable {
        throw new UnsupportedOperationException("shouldn't be called");
    }
}
