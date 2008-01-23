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
import java.util.Collection;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Render a collection of monitor for reporting
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRenderer
    implements Renderer
{
    private final Collection<String> roles;

    private final PrintWriter writer;

    public AbstractRenderer( PrintWriter writer, Collection<String> roles )
    {
        super();
        this.roles = roles;
        this.writer = writer;
    }

    protected void write( String string )
    {
        writer.append( string );
    }

    public void render( Collection<Monitor> monitors )
    {
        for ( Monitor monitor : monitors )
        {
            render( monitor );
        }
    }

    public void render( Monitor monitor )
    {
        render( monitor.getKey() );
        for ( String role : roles )
        {
            render( monitor.getValue( role ), role );
        }
    }

    public abstract void render( StatValue value, String role );

    public abstract void render( Key key );
}
