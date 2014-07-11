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
package org.apache.sirona.reporting.web.handler.internal;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.reporting.web.handler.TemplateHelper;
import org.apache.sirona.reporting.web.plugin.api.Regex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EndpointInfo {
    private final Map<Pattern, Invoker> invokers = new HashMap<Pattern, Invoker>();

    public Map<Pattern, Invoker> getInvokers() {
        return invokers;
    }

    public static EndpointInfo build(final Class<?> endpointClass, final String id, final String rootMapping) {
        final EndpointInfo info = new EndpointInfo();
        final Object instance = IoCs.newInstance(endpointClass);
        for (final Method method : endpointClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            final Regex regex = method.getAnnotation(Regex.class);
            if (regex != null) {
                final Pattern pattern = Pattern.compile(rootMapping + regex.value());
                final Invoker invoker = new Invoker(instance, method, id);
                int partIdx = 1; // regex index, it starts from 1
                for (final Class<?> clazz : method.getParameterTypes()) {
                    if (HttpServletRequest.class.equals(clazz)) {
                        invoker.addRequestParameter();
                    } else if (HttpServletResponse.class.equals(clazz)) {
                        invoker.addResponseParameter();
                    } else if (TemplateHelper.class.equals(clazz)) {
                        invoker.addTemplateHelper(id);
                    } else {
                        invoker.addSegmentParameter(clazz, partIdx);
                        partIdx++;
                    }
                }
                info.invokers.put(pattern, invoker);
            }
        }
        return info;
    }
}
