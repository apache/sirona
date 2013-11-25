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
package org.apache.sirona.agent.jaxrs.cxf;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.message.Message;
import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class CxfJaxRsPerformanceHandler extends AbstractPerformanceInterceptor<Message> implements RequestHandler, ResponseHandler {
    public static final Role ROLE = new Role(Role.PERFORMANCES.getName() + "-cxf26", Role.PERFORMANCES.getUnit());

    private static final String PROP_KEY = Context.class.getName();
    private static final String NO_METHOD = "X";

    @Override
    public Response handleRequest(final Message m, final ClassResourceInfo resourceClass) {
        final Object rest = m.getContextualProperty(Message.REST_MESSAGE);
        if (rest != null && Boolean.TRUE.equals(rest)) {
            m.getExchange().put(PROP_KEY, before(m, getCounterName(m)));
        }
        return null;
    }

    @Override
    public Response handleResponse(final Message m, final OperationResourceInfo ori, final Response response) {
        final Context context = Context.class.cast(m.getExchange().get(PROP_KEY));
        if (context != null) {
            context.stop();
        }
        return null;
    }

    @Override
    protected String getCounterName(final Message message) {
        final OperationResourceInfo ori = message.getExchange().get(OperationResourceInfo.class);

        String method = ori.getHttpMethod();
        if (method == null) {
            method = NO_METHOD;
        }

        final StringBuilder builder = new StringBuilder(method).append("-");
        builder.append(message.getContextualProperty("org.apache.cxf.message.Message.BASE_PATH"));
        final URITemplate classTpl = ori.getClassResourceInfo().getURITemplate();
        if (classTpl != null) {
            builder.append(classTpl.getValue());
        }
        builder.append(ori.getURITemplate().getValue());

        return builder.toString().replace("//", "/");
    }

    @Override
    protected Object proceed(final Message invocation) throws Throwable {
        throw new UnsupportedOperationException("shouldn't be called");
    }

    @Override
    protected Role getRole() {
        return ROLE;
    }
}
