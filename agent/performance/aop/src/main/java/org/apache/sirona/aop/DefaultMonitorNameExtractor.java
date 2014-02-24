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

package org.apache.sirona.aop;

import java.lang.reflect.Method;

/**
 *
 */
public class DefaultMonitorNameExtractor implements MonitorNameExtractor {
    // singleton to avoid to need too much memory for nothing
    public static final DefaultMonitorNameExtractor INSTANCE = new DefaultMonitorNameExtractor();

    private DefaultMonitorNameExtractor() {
        // no-op
    }

    public String getMonitorName(final Object instance, final Method method) {
        if (instance == null) { // static method
            return method.getDeclaringClass().getName() + "." + method.getName();
        } else {
            return instance.getClass().getName() + "." + method.getName();
        }
    }

}
