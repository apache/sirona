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

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

public class JsonRenderer
    extends AbstractRenderer
{
    @Override
    public void render( PrintWriter writer, Collection<Monitor> monitors, Filter filter )
    {
        writer.append( "[" );
        super.render( writer, monitors, filter );
        writer.append( "]" );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void hasNext( PrintWriter writer, Class type )
    {
        writer.append( ',' );
    }

    @Override
    public void render( PrintWriter writer, Monitor monitor, Filter filter )
    {
        writer.append( "{" );
        if ( renderStatValues( writer, monitor, filter ) > 0 )
        {
            writer.append( "," );
        }
        render( writer, monitor.getKey() );
        writer.append( "}" );
    }

    @Override
    public void render( PrintWriter writer, Key key )
    {
        writer.append( "key:{name:\"" );
        writer.append( key.getName() );
        if ( key.getCategory() != null )
        {
            writer.append( "\",category:\"" );
            writer.append( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            writer.append( "\",subsystem:\"" );
            writer.append( key.getSubsystem() );
        }
        writer.append( "\"}" );
    }

    @Override
    public void render( PrintWriter writer, StatValue value )
    {
        writer.append( value.getRole() );
        writer.append( ":{value:\"" );
        writer.append( String.valueOf( value.get() ) );
        writer.append( "\",min:\"" );
        writer.append( String.valueOf( value.getMin() ) );
        writer.append( "\",max:\"" );
        writer.append( String.valueOf( value.getMax() ) );
        writer.append( "\",mean:\"" );
        writer.append( String.valueOf( value.getMean() ) );
        writer.append( "\",stdDev:\"" );
        writer.append( String.valueOf( value.getStandardDeviation() ) );
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            writer.append( "\",total:\"" );
            writer.append( String.valueOf( counter.getSum() ) );
            writer.append( "\",hits:\"" );
            writer.append( String.valueOf( counter.getHits() ) );
        }
        writer.append( "\"}" );
    }

}
