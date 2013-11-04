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
package org.apache.sirona.reporting.web.plugin.jmx;

import org.apache.sirona.reporting.web.plugin.Local;
import org.apache.sirona.reporting.web.plugin.Plugin;

@Local
public class JMXPlugin implements Plugin {
    @Override
    public String name() {
        return "JMX";
    }

    @Override
    public Class<?> endpoints() {
        return JMXEndpoints.class;
    }

    @Override
    public String mapping() {
        return "/jmx";
    }
}
