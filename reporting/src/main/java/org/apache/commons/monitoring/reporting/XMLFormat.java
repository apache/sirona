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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.monitoring.Monitor;

/**
 * Format to XML, with optional indentation
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class XMLFormat
    implements Format
{
    public XMLFormat( boolean indent )
    {
        super();
        this.indent = indent;
    }

    private boolean indent;

    public void repositoryStart( PrintWriter writer )
    {
        writer.append( "<repository>" );
    }

    public void repositoryEnd( PrintWriter writer )
    {
        if ( indent )
        {
            writer.append( "\n" );
        }
        writer.append( "</repository>" );
    }

    public void monitorStart( PrintWriter writer, Monitor monitor )
    {
        if ( indent )
        {
            writer.append( "\n  " );
        }
        writer.append( "<monitor" );
        Monitor.Key key = monitor.getKey();
        attribute( writer, "name", key.getName() );
        attribute( writer, "category", key.getCategory() );
        attribute( writer, "subsystem", key.getSubsystem() );
        writer.append( '>' );
    }

    public void monitorEnd( PrintWriter writer, String name )
    {
        if ( indent )
        {
            writer.append( "\n  " );
        }
        writer.append( "</monitor>" );
    }

    public void gaugeStart( PrintWriter writer, String name )
    {
        if ( indent )
        {
            writer.append( "\n    " );
        }
        writer.append( "<gauge" );
        attribute( writer, "role", name );
    }

    public void gaugeEnd( PrintWriter writer, String name )
    {
        writer.append( "/>" );
    }

    public void counterStart( PrintWriter writer, String name )
    {
        if ( indent )
        {
            writer.append( "\n    " );
        }
        writer.append( "<counter" );
        attribute( writer, "role", name );
    }

    public void counterEnd( PrintWriter writer, String name )
    {
        writer.append( "/>" );
    }

    public void attribute( PrintWriter writer, String name, String value )
    {
        writer.append( " " )
              .append( name )
              .append( "=\"" );
        escape( writer, value );
        writer.append( "\"" );
    }

    public void escape( PrintWriter writer, String string )
    {
        writer.append( StringEscapeUtils.escapeXml( string ) );
    }

    public void separator( PrintWriter writer )
    {
        // Nop
    }
}