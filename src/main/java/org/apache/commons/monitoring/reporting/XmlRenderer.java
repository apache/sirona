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

public class XmlRenderer
    extends AbstractRenderer
{

    public XmlRenderer( PrintWriter writer, Collection<String> roles )
    {
        super( writer, roles );
    }

    @Override
    public void render( Collection<Monitor> monitors )
    {
        write( "<monitors>" );
        super.render( monitors );
        write( "</monitors>" );
    }

    @Override
    public void render( Monitor monitor )
    {
        write( "<monitor " );
        super.render( monitor );
        write( "</monitor>" );
    }

    @Override
    public void render( Key key )
    {
        write( "name=\"" );
        write( key.getName() );
        if ( key.getCategory() != null )
        {
            write( "\" category=\"" );
            write( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            write( "\" subsystem=\"" );
            write( key.getSubsystem() );
        }
        write( "\">" );
    }

    @Override
    public void render( StatValue value, String role )
    {
        write( "<" );
        write( role );

        write( " value=\"" );
        write( String.valueOf( value.get() ) );
        write( "\" min=\"" );
        write( String.valueOf( value.min() ) );
        write( "\" max=\"" );
        write( String.valueOf( value.max() ) );
        write( "\" average=\"" );
        write( String.valueOf( value.average() ) );
        write( "\" stdDev=\"" );
        write( String.valueOf( value.standardDeviation() ) );
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            write( "\" total=\"" );
            write( String.valueOf( counter.total() ) );
            write( "\" hits=\"" );
            write( String.valueOf( counter.hits() ) );
        }
        write( "\"/>" );
    }

}
