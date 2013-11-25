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

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Provider
public class PerformanceServerFilter extends AbstractPerformanceInterceptor<ContainerRequestContext> implements ContainerRequestFilter, ContainerResponseFilter {
    public static final Role ROLE = new Role(Role.PERFORMANCES.getName() + "-jaxrs2", Role.PERFORMANCES.getUnit());

    private static final String SIRONA_PERFORMANCE_PROP = "sirona-performance";
    private static final String NO_METHOD = "X";
    private final ConcurrentMap<Method, Mapping> mappings = new ConcurrentHashMap<Method, Mapping>();

    @javax.ws.rs.core.Context
    private ResourceInfo info;

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        context.setProperty(SIRONA_PERFORMANCE_PROP, before(context, getCounterName(context)));
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        final Context context = Context.class.cast(requestContext.getProperty(SIRONA_PERFORMANCE_PROP));
        if (context != null) {
            context.stop();
        }
    }

    @Override
    protected String getCounterName(final ContainerRequestContext context) {
        final String base = context.getUriInfo().getBaseUri().getPath();
        final Method rm = info.getResourceMethod();
        if (rm == null) {
            return base + "?";
        }

        Mapping mapping = mappings.get(rm);

        if (mapping == null) {
            String method = context.getMethod();
            if (method == null) {
                method = NO_METHOD;
            }

            final StringBuilder builder = new StringBuilder();

            final Class<?> rc = info.getResourceClass();
            if (rc != null && rc.getAnnotation(Path.class) != null) {
                builder.append(rc.getAnnotation(Path.class).value());
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '/') {
                    builder.append('/');
                }
            }

            if (rm.getAnnotation(Path.class) != null) {
                builder.append(rm.getAnnotation(Path.class).value());
            }

            mapping = new Mapping(method, builder.toString());
            mappings.putIfAbsent(rm, mapping);
        }

        return mapping.map(base);
    }

    @Override
    protected Role getRole() {
        return ROLE;
    }

    @Override
    protected Object proceed(final ContainerRequestContext context) throws Throwable {
        throw new UnsupportedOperationException("shouldn't be called");
    }

    protected static class Mapping {
        protected final String method;
        protected final String path;

        protected Mapping(final String method, final String path) {
            this.method = method + '-';
            this.path = path;
        }

        public String map(final String base) {
            return method + base + path;
        }
    }
}
