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

package org.apache.commons.monitoring;

import java.util.Collection;
import java.util.EventListener;
import java.util.Set;

import org.apache.commons.monitoring.Monitor.Key;

/**
 * The repository maintains a set of monitors and ensure unicity. It creates monitors on-demand
 * based on requested Keys. After creation, the monitor Key cannot be updated.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Repository
{
    /**
     * Retrieve or create a monitor it's key
     */
    Monitor getMonitor( Monitor.Key key );

    /**
     * Retrieve or create a monitor by name
     */
    Monitor getMonitor( String name );

    /**
     * Retrieve or create a monitor by name and category
     */
    Monitor getMonitor( String name, String category );

    /**
     * Retrieve or create a monitor by name, category and subsystem
     */
    Monitor getMonitor( String name, String category, String subsystem );

    /**
     * @return all monitors registered in the repository
     */
    Collection<Monitor> getMonitors();

    /**
     * @param category a category name
     * @return all monitors in the repository that declare this category in
     * there Key
     */
    Collection<Monitor> getMonitorsFromCategory( String category );

    /**
     * @param subsystem a subsystem name
     * @return all monitors in the repository that declare this subsystem in
     * there Key
     */
    Collection<Monitor> getMonitorsFromSubSystem( String subsystem );

    /**
     * @return the categories declared by monitors in the repository
     */
    Set<String> getCategories();

    /**
     * @return the subsystems declared by monitors in the repository
     */
    Set<String> getSubSystems();

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
     * @param monitor the monitor associated with the process
     * @return a running StopWatch
     */
    StopWatch start( Monitor monitor );

    /**
     * A repository that support the Observer pattern.
     */
    public interface Observable extends Repository
    {
        /**
         * @param listener listener to get registered
         */
        void addListener( Listener listener );

        /**
         * @param listener listener to get removed
         */
        void removeListener( Listener listener );

    }

    /**
     * Listener interface to get notified on repository events
     */
    public static interface Listener
        extends EventListener
    {
        /**
         * A monitor has just been created. Can be used to add custom Metrics or
         * to register Metric.Listener for all monitors that declare the same category or
         * subsystem.
         *
         * @param monitor
         */
        void newMonitorInstance( Monitor monitor );
    }

}