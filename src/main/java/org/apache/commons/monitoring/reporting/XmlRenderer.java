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

public class XmlRenderer
    extends AbstractRenderer
{

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.PrintWriter, java.util.Collection, org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    public void render( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        writer.print( "<monitors>" );
        super.render( writer, monitors, options );
        writer.print( "</monitors>" );
    }

    @Override
    public void render( PrintWriter writer, Monitor monitor, Options options )
    {
        writer.print( "<monitor " );
        super.render( writer, monitor, options );
        writer.print( "</monitor>" );
    }

    @Override
    public void render( PrintWriter writer, Key key )
    {
        writer.print( "name=\"" );
        writer.print( key.getName() );
        if ( key.getCategory() != null )
        {
            writer.print( "\" category=\"" );
            writer.print( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            writer.print( "\" subsystem=\"" );
            writer.print( key.getSubsystem() );
        }
        writer.print( "\">" );
    }


    @Override
    public void render( PrintWriter writer, StatValue value, Options options )
    {
        writer.print( "<" );
        writer.print( value.getRole() );
        super.render( writer, value, options );
        writer.print( "/>" );
    }

    @Override
    protected void render( PrintWriter writer, StatValue value, String attribute, Number number, Options options )
    {
        writer.print( ' ' );
        writer.print( attribute );
        writer.print( "=\"" );
        super.render( writer, value, attribute, number, options );
        writer.print( '\"' );
    }

}
