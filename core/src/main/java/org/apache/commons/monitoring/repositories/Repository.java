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

package org.apache.commons.monitoring.repositories;

import org.apache.commons.monitoring.Visitable;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.util.Collection;
import java.util.Set;

/**
 * The repository maintains a set of monitors and ensure unicity. It creates monitors on-demand
 * based on requested Keys. After creation, the monitor Key cannot be updated.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Repository extends Visitable {
    static final Repository INSTANCE = Configuration.newInstance(Repository.class);

    /**
     * Retrieve or create a monitor it's key
     */
    Monitor getMonitor(Monitor.Key key);

    /**
     * Retrieve or create a monitor by name
     */
    Monitor getMonitor(String name);

    /**
     * Retrieve or create a monitor by name and category
     */
    Monitor getMonitor(String name, String category);

    /**
     * @return all monitors registered in the repository
     */
    Collection<Monitor> getMonitors();

    /**
     * @param category a category name
     * @return all monitors in the repository that declare this category in
     * there Key
     */
    Collection<Monitor> getMonitorsFromCategory(String category);

    /**
     * @return the categories declared by monitors in the repository
     */
    Set<String> getCategories();

    /**
     * Reset the repository : all existing monitors are destroyed and data are lost.
     */
    void clear();

    /**
     * Convenience method to reset all monitors (don't remove them from repository)
     */
    void reset();


    /**
     * Start a StopWatch to monitor execution
     *
     * @param monitor the monitor associated with the process
     * @return a running StopWatch
     */
    StopWatch start(Monitor monitor);
}
