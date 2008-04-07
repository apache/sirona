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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.listeners.Detachable;
import org.apache.commons.monitoring.listeners.SecondaryMonitor;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HtmlRenderer
    extends AbstractRenderer
{

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.Context,
     * java.util.Collection,
     * org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    public void render( Context ctx, Collection<Monitor> monitors, Options options )
    {
        prepareRendering( ctx, monitors, options );
        documentHead( ctx );
        tableStartTag( ctx );
        tableHead( ctx, monitors, options );
        tabelBody( ctx, monitors, options );
        tableEndTag( ctx );
        documentFoot( ctx );
    }

    protected void tabelBody( Context ctx, Collection<Monitor> monitors, Options options )
    {
        ctx.println( "<tbody><tr>" );
        super.render( ctx, monitors, options );
        ctx.println( "</tr></tbody>" );
    }

    /**
     * @param ctx
     */
    protected void tableStartTag( Context ctx )
    {
        ctx.print( "<table border='1'>" );
    }

    /**
     * @param ctx
     */
    protected void tableEndTag( Context ctx )
    {
        ctx.println( "</table>" );
    }

    /**
     * @param ctx
     */
    protected void documentHead( Context ctx )
    {
        ctx.println( "<html><body>" );
    }

    @SuppressWarnings( "unchecked" )
    protected void tableHead( Context ctx, Collection<Monitor> monitors, Options options )
    {
        Collection<String> roles = (Collection<String>) ctx.get( "roles" );
        Map<String, Integer> columns = new HashMap<String, Integer>();

        ctx.println( "<thead><tr>" );
        boolean detached = ( monitors.size() > 0 && monitors.iterator().next()instanceof Detachable );

        if ( detached )
        {
            ctx.println( "<td colspan='2'>period</td>" );
        }
        ctx.println( "<th rowspan='2'>name</th>" );
        ctx.println( "<th rowspan='2'>category</th>" );
        ctx.println( "<th rowspan='2'>subsystem</th>" );
        for ( String role : roles )
        {
            // Search the first monitor that has a StatValue for the role...
            for ( Monitor monitor : monitors )
            {
                StatValue value = monitor.getValue( role );
                if ( value != null )
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

                    ctx.print( "<td colspan='" );
                    ctx.print( String.valueOf( span ) );
                    ctx.print( "'>" );
                    ctx.print( value.getRole() );
                    Unit unit = options.unitFor( value );
                    if ( unit != null && unit.getName().length() > 0 )
                    {
                        renderUnit( ctx, unit );
                    }
                    ctx.print( "</td>" );
                    columns.put( role, span );
                    break;
                }
            }
        }
        ctx.print( "</tr>" );

        ctx.print( "<tr>" );
        if ( detached )
        {
            ctx.println( "<th>from</th>" );
            ctx.println( "<th>to</th>" );
        }

        for ( String role : roles )
        {
            for ( Monitor monitor : monitors )
            {
                StatValue value = monitor.getValue( role );
                if ( value != null )
                {
                    if ( value instanceof Counter )
                    {
                        writeColumnHead( ctx, options, value, "hits" );
                        writeColumnHead( ctx, options, value, "sum" );
                    }
                    writeColumnHead( ctx, options, value, "min" );
                    writeColumnHead( ctx, options, value, "max" );
                    writeColumnHead( ctx, options, value, "mean" );
                    writeColumnHead( ctx, options, value, "deviation" );
                    writeColumnHead( ctx, options, value, "value" );
                    break;
                }
            }
        }
        ctx.println( "</tr></thead>" );
        ctx.put( "columns", columns );
    }

    protected void writeColumnHead( Context ctx, Options options, StatValue value, String attribute )
    {
        if ( options.render( value, attribute ) )
        {
            ctx.print( "<th>" );
            ctx.print( attribute );
            ctx.print( "</th>" );
        }
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#renderDetached(org.apache.commons.monitoring.reporting.Context, org.apache.commons.monitoring.listeners.SecondaryMonitor, org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    protected void renderDetached( Context ctx, Detachable detached, Options options )
    {
        ctx.print( "<td>" );
        ctx.print( options.getDateFormat().format( new Date( detached.getAttachedAt()) ) );
        ctx.print( "</td><td>" );
        ctx.print( options.getDateFormat().format( new Date( detached.getDetachedAt()) ) );
        ctx.print( "</td>" );
    }

    protected void renderUnit( Context ctx, Unit unit )
    {
        ctx.print( " (" );
        ctx.print( unit.getName() );
        ctx.print( ")" );
    }

    @Override
    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        ctx.print( "<td>" );
        super.render( ctx, value, attribute, number, options, ratio );
        ctx.print( "</td>" );
    }

    @Override
    protected void render( Context ctx, Key key )
    {
        ctx.print( "<td>" );
        ctx.print( key.getName() );
        ctx.print( "</td><td>" );
        if ( key.getCategory() != null )
        {
            ctx.print( key.getCategory() );
        }
        ctx.print( "</td><td>" );
        if ( key.getSubsystem() != null )
        {
            ctx.print( key.getSubsystem() );
        }
        ctx.print( "</td>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#renderMissingValue(org.apache.commons.monitoring.reporting.Context,
     * java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void renderMissingValue( Context ctx, String role )
    {
        Map<String, Integer> columns = (Map<String, Integer>) ctx.get( "columns" );
        ctx.print( "<td colspan='" );
        ctx.print( String.valueOf( columns.get( role ) ) );
        ctx.print( "'>-</td>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#hasNext(java.io.Context,
     * java.lang.Class)
     */
    @Override
    protected void hasNext( Context ctx, Class<?> type )
    {
        if ( type == Monitor.class )
        {
            ctx.println( "</tr>" );
            ctx.println( "<tr>" );
        }
    }

    /**
     * @param ctx
     */
    protected void documentFoot( Context ctx )
    {
        ctx.print( "</body></html>" );
    }

}
