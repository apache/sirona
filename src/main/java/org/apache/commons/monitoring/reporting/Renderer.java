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

package org.apache.commons.monitoring.reporting;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;

/**
 * Render a collection of monitor for reporting
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Renderer
{
    Collection<String> DEFAULT_ROLES = Arrays.asList( new String[] { Monitor.CONCURRENCY, Monitor.PERFORMANCES } );

    void render( PrintWriter writer, Collection<Monitor> monitors );

    void render( PrintWriter writer, Collection<Monitor> monitors, Filter filter );

    interface Filter
    {
        boolean render( Object object );
    }

    /**
     * Filter implementation to render only a selection of StatValues identified by roles
     */
    class RoleFilter
        implements Filter
    {
        private Collection<String> roles;

        public RoleFilter( Collection<String> roles )
        {
            this.roles = roles;
        }

        public RoleFilter( String[] roles )
        {
            this.roles = Arrays.asList( roles );
        }

        public boolean render( Object object )
        {
            if ( object instanceof StatValue )
            {
                return this.roles.contains( ( (StatValue) object ).getRole() );
            }
            return true;
        }
    }
}
