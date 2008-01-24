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
import java.util.Iterator;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

public class JsonRenderer
    extends AbstractRenderer
{
    private final Collection<String> roles;

    public JsonRenderer( PrintWriter writer, Collection<String> roles )
    {
        super( writer, roles );
        this.roles = roles;
    }

    @Override
    public void render( Collection<Monitor> monitors )
    {
        write( "[" );
        for ( Iterator<Monitor> iterator = monitors.iterator(); iterator.hasNext(); )
        {
            Monitor monitor = iterator.next();
            render( monitor );
            if ( iterator.hasNext() )
            {
                write( "," );
            }
        }
        write( "]" );
    }

    @Override
    public void render( Monitor monitor )
    {
        write( "{" );
        render( monitor.getKey() );
        if ( !roles.isEmpty() )
        {
            write( "," );
        }
        for ( Iterator<String> iterator = roles.iterator(); iterator.hasNext(); )
        {
            String role = iterator.next();
            render( monitor.getValue( role ), role );
            if ( iterator.hasNext() )
            {
                write( "," );
            }
        }
        write( "}" );
    }

    @Override
    public void render( Key key )
    {
        write( "key:{name:\"" );
        write( key.getName() );
        if ( key.getCategory() != null )
        {
            write( "\",category:\"" );
            write( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            write( "\",subsystem:\"" );
            write( key.getSubsystem() );
        }
        write( "\"}" );
    }

    @Override
    public void render( StatValue value, String role )
    {
        write( role );
        write( ":{value:\"" );
        write( String.valueOf( value.get() ) );
        write( "\",min:\"" );
        write( String.valueOf( value.getMin() ) );
        write( "\",max:\"" );
        write( String.valueOf( value.getMax() ) );
        write( "\",mean:\"" );
        write( String.valueOf( value.getMean() ) );
        write( "\",stdDev:\"" );
        write( String.valueOf( value.getStandardDeviation() ) );
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            write( "\",total:\"" );
            write( String.valueOf( counter.getSum() ) );
            write( "\",hits:\"" );
            write( String.valueOf( counter.getHits() ) );
        }
        write( "\"}" );
    }

}
