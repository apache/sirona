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

public class JsonRenderer
    extends AbstractRenderer
{
    @Override
    public void render( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        writer.print( "[" );
        super.render( writer, monitors, options );
        writer.print( "]" );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void hasNext( PrintWriter writer, Class type )
    {
        writer.print( ',' );
    }

    @Override
    public void render( PrintWriter writer, Monitor monitor, Options options )
    {
        writer.print( "{" );
        if ( renderStatValues( writer, monitor, options ) > 0 )
        {
            writer.print( "," );
        }
        render( writer, monitor.getKey() );
        writer.print( "}" );
    }

    @Override
    public void render( PrintWriter writer, Key key )
    {
        writer.print( "key:{name:\"" );
        writer.print( key.getName() );
        if ( key.getCategory() != null )
        {
            writer.print( "\",category:\"" );
            writer.print( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            writer.print( "\",subsystem:\"" );
            writer.print( key.getSubsystem() );
        }
        writer.print( "\"}" );
    }

    @Override
    public void render( PrintWriter writer, StatValue value, Options options )
    {
        writer.print( value.getRole() );
        writer.print( ":{" );
        super.render( writer, value, options );
        writer.print( "}" );
    }

    /** Current rendering state */
    private StatValue currentValue;
    private boolean firstAttribute;

    @Override
    protected void render( PrintWriter writer, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        if (currentValue != value)
        {
            currentValue = value;
            firstAttribute = true;
        }

        if (!firstAttribute)
        {
            writer.print( ',' );
        }
        writer.print( attribute );
        writer.print( ":\"" );
        super.render( writer, value, attribute, number, options, ratio );
        writer.print( '\"' );
        firstAttribute = false;
    }
}
