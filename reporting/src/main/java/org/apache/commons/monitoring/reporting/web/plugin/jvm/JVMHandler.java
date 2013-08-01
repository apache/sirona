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
package org.apache.commons.monitoring.reporting.web.plugin.jvm;

import org.apache.commons.monitoring.reporting.web.handler.HandlerRendererAdapter;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.repositories.Repository;

import java.util.Map;

public class JVMHandler extends HandlerRendererAdapter {
    protected String getTemplate() {
        return "jvm/jvm.vm";
    }

    protected Map<String,?> getVariables() {
        return new MapBuilder<String, Object>()
            .set("cpu", Repository.INSTANCE.getCounter(JVMPlugin.CPU_KEY).getMean())
            .set("memory", Repository.INSTANCE.getCounter(JVMPlugin.MEMORY_KEY).getMean())
            .build();
    }
}
