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
import java.util.List;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HtmlRenderer
    extends AbstractRenderer
{

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.PrintWriter,
     * java.util.Collection,
     * org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    public void render( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        documentHead( writer );
        tableStartTag( writer );
        tableHead( writer, monitors, options );
        tabelBody( writer, monitors, options );
        tableEndTag( writer );
        documentFoot( writer );
    }

    protected void tabelBody( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        writer.println( "<tbody><tr>" );
        super.render( writer, monitors, options );
        writer.println( "</tr></tbody>" );
    }

    /**
     * @param writer
     */
    protected void tableStartTag( PrintWriter writer )
    {
        writer.print( "<table border='1'>" );
    }

    /**
     * @param writer
     */
    protected void tableEndTag( PrintWriter writer )
    {
        writer.println( "</table>" );
    }

    /**
     * @param writer
     */
    protected void documentHead( PrintWriter writer )
    {
        writer.println( "<html><body>" );
    }

    protected void tableHead( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        Iterator<Monitor> it = monitors.iterator();
        if ( it.hasNext() )
        {
            Monitor monitor = it.next();
            writer
                .println( "<thead><tr><th rowspan='2'>name</th><th rowspan='2'>category</th><th rowspan='2'>subsystem</th>" );
            List<StatValue> values = getOrderedStatValues( monitor, options );
            for ( StatValue value : values )
            {
                int span = 0;
                if ( value instanceof Counter )
                {
                    span += options.render( value, "hits" ) ? 1 : 0;
                    span += options.render( value, "sum" ) ? 1 : 0;
                }
                span += options.render( value, "min" ) ? 1 : 0;
                span += options.render( value, "max" ) ? 1 : 0;
                span += options.render( value, "mean" ) ? 1 : 0;
                span += options.render( value, "deviation" ) ? 1 : 0;
                span += options.render( value, "value" ) ? 1 : 0;

                writer.print( "<td colspan='" );
                writer.print( String.valueOf( span ) );
                writer.print( "'>" );
                writer.print( value.getRole() );
                Unit unit = options.unitFor( value );
                if ( unit != null && unit.getName().length() > 0 )
                {
                    renderUnit( writer, unit );
                }
                writer.print( "</td>" );
            }
            writer.print( "</tr>" );
            writer.print( "<tr>" );
            for ( StatValue value : values )
            {
                if ( value instanceof Counter )
                {
                    writeColumnHead( writer, options, value, "hits" );
                    writeColumnHead( writer, options, value, "sum" );
                }
                writeColumnHead( writer, options, value, "min" );
                writeColumnHead( writer, options, value, "max" );
                writeColumnHead( writer, options, value, "mean" );
                writeColumnHead( writer, options, value, "deviation" );
                writeColumnHead( writer, options, value, "value" );
            }
            writer.println( "</tr></thead>" );
        }
    }

    protected void writeColumnHead( PrintWriter writer, Options options, StatValue value, String attribute )
    {
        if ( options.render( value, attribute ) )
        {
            writer.print( "<th>" );
            writer.print( attribute );
            writer.print( "</th>" );
        }
    }

    protected void renderUnit( PrintWriter writer, Unit unit )
    {
        writer.print( " (" );
        writer.print( unit.getName() );
        writer.print( ")" );
    }

    @Override
    protected void render( PrintWriter writer, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        writer.print( "<td>" );
        super.render( writer, value, attribute, number, options, ratio );
        writer.print( "</td>" );
    }

    @Override
    protected void render( PrintWriter writer, Key key )
    {
        writer.print( "<td>" );
        writer.print( key.getName() );
        writer.print( "</td><td>" );
        if ( key.getCategory() != null )
        {
            writer.print( key.getCategory() );
        }
        writer.print( "</td><td>" );
        if ( key.getSubsystem() != null )
        {
            writer.print( key.getSubsystem() );
        }
        writer.print( "</td>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#hasNext(java.io.PrintWriter,
     * java.lang.Class)
     */
    @Override
    protected void hasNext( PrintWriter writer, Class<?> type )
    {
        if ( type == Monitor.class )
        {
            writer.println( "</tr>" );
            writer.println( "<tr>" );
        }
    }

    /**
     * @param writer
     */
    protected void documentFoot( PrintWriter writer )
    {
        writer.print( "</body></html>" );
    }

}
