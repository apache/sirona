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
import java.util.Set;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Repository
{
    Monitor getMonitor( String name );

    Monitor getMonitor( String name, String category );

    Monitor getMonitor( String name, String category, String subsystem );

    /**
     * @return all monitors registered in the repository
     */
    public Collection<Monitor> getMonitors();

    /**
     * @param category a category name
     * @return all monitors in the repository that declare this category in
     * there Key
     */
    public Collection<Monitor> getMonitorsFromCategory( String category );

    /**
     * @param subsystem a subsystem name
     * @return all monitors in the repository that declare this subsystem in
     * there Key
     */
    public Collection<Monitor> getMonitorsFromSubSystem( String subsystem );

    /**
     * @return the categories declared by monitors in the repository
     */
    public Set<String> getCategories();

    /**
     * @return the subsystems declared by monitors in the repository
     */
    public Set<String> getSubSystems();

}
