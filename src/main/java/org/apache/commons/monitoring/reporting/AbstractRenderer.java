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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Render a collection of monitor for reporting
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRenderer
    implements Renderer
{
    public final void render( Context ctx, Collection<Monitor> monitors )
    {
        render( ctx, monitors, new OptionsSupport() );
    }

    public void render( Context ctx, Collection<Monitor> monitors, Options options )
    {
        int count = 0;
        prepareRendering( ctx, monitors, options );
        for ( Monitor monitor : monitors )
        {
            if ( options.render( monitor ) )
            {
                if ( count > 0 )
                {
                    hasNext( ctx, Monitor.class );
                }
                render( ctx, monitor, options );
                count++;
            }
        }
    }

    protected void prepareRendering( Context ctx, Collection<Monitor> monitors, Options options )
    {
        List<String> roles = getRoles( monitors, options );
        ctx.put( "roles", roles );
        ctx.put( "monitors", monitors );
    }

    protected void hasNext( Context ctx, Class<?> type )
    {
        // Nop
    }

    protected void render( Context ctx, Monitor monitor, Options options, List<String> roles )
    {
        render( ctx, monitor, options );
    }

    protected void render( Context ctx, Monitor monitor, Options options )
    {
        render( ctx, monitor.getKey() );
        renderStatValues( ctx, monitor, options );
    }

    protected void renderStatValues( Context ctx, Monitor monitor, Options options, List<String> roles )
    {
        renderStatValues( ctx, monitor, options );
    }

    @SuppressWarnings("unchecked")
    protected void renderStatValues( Context ctx, Monitor monitor, Options options )
    {
        List<String> roles = (List<String>) ctx.get( "roles" );
        for ( Iterator<String> iterator = roles.iterator(); iterator.hasNext(); )
        {
            String role = iterator.next();
            StatValue value = monitor.getValue( role );
            if (value != null)
            {
                render( ctx, value, options );
            }
            else
            {
                renderMissingValue( ctx, role );
            }
            if ( iterator.hasNext() )
            {
                hasNext( ctx, StatValue.class );
            }
        }
    }

    /**
     * Render an expected value not supported by the current monitor
     * @param ctx
     * @param role
     */
    protected void renderMissingValue( Context ctx, String role )
    {
        // Nop
    }

    protected List<StatValue> getOrderedStatValues( Monitor monitor, Options options )
    {
        List<StatValue> values = new LinkedList<StatValue>( monitor.getValues() );
        for ( Iterator<StatValue> iterator = values.iterator(); iterator.hasNext(); )
        {
            StatValue value = (StatValue) iterator.next();
            if ( !options.renderRole( value.getRole() ) )
            {
                iterator.remove();
            }
        }
        Collections.sort( values, new Comparator<StatValue>()
        {
            public int compare( StatValue value1, StatValue value2 )
            {
                return value1.getRole().compareTo( value2.getRole() );
            }
        } );
        return values;
    }

    protected void render( Context ctx, StatValue value, Options options )
    {
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            if ( options.render( value, "hits" ) )
            {
                render( ctx, value, "hits", counter.getHits(), options, 0 );
            }
            if ( options.render( value, "sum" ) )
            {
                render( ctx, value, "sum", counter.getSum(), options );
            }
        }
        if ( options.render( value, "min" ) )
        {
            render( ctx, value, "min", value.getMin(), options );
        }
        if ( options.render( value, "max" ) )
        {
            render( ctx, value, "max", value.getMax(), options );
        }
        if ( options.render( value, "mean" ) )
        {
            render( ctx, value, "mean", value.getMean(), options );
        }
        if ( options.render( value, "deviation" ) )
        {
            render( ctx, value, "deviation", value.getStandardDeviation(), options, 1 );
        }
        if ( options.render( value, "value" ) )
        {
            render( ctx, value, "value", value.get(), options, 1 );
        }
    }

    protected abstract void render( Context ctx, Key key );

    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options )
    {
        render( ctx, value, attribute, number, options, 1 );
    }

    /**
     * Render a StatValue attribute
     *
     * @param ctx output
     * @param value the StatValue that hold data to be rendered
     * @param attribute the StatValue attribute name to be rendered
     * @param number the the StatValue attribute value to be rendered
     * @param ratio the ratio between attribute unit and statValue unit (in power of 10)
     * @param options the rendering options
     */
    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options,
                           int ratio )
    {
        if ( number instanceof Double )
        {
            renderInternal( ctx, value, attribute, number.doubleValue(), options, ratio );
        }
        else
        {
            renderInternal( ctx, value, attribute, number.longValue(), options, ratio );
        }
    }

    private void renderInternal( Context ctx, StatValue value, String attribute, long l, Options options,
                                 int ratio )
    {
        Unit unit = options.unitFor( value );
        if ( unit != null )
        {
            while ( ratio-- > 0 )
            {
                l = l / unit.getScale();
            }
        }

        ctx.print( options.getNumberFormat().format( l ) );
    }

    private void renderInternal( Context ctx, StatValue value, String attribute, double d, Options options,
                                 int ratio )
    {
        if ( Double.isNaN( d ) )
        {
            ctx.print( "-" );
            return;
        }
        Unit unit = options.unitFor( value );
        if ( unit != null )
        {
            while ( ratio-- > 0 )
            {
                d = d / unit.getScale();
            }
        }

        ctx.print( options.getDecimalFormat().format( d ) );
    }

    /**
     * @param monitors
     * @return
     */
    protected List<String> getRoles( Collection<Monitor> monitors, Options options )
    {
        Set<String> roles = new HashSet<String>();
        for ( Monitor monitor : monitors )
        {
            if (options.render( monitor ))
            {
                for ( String role : monitor.getRoles() )
                {
                    if (options.renderRole( role ))
                    {
                        roles.add( role );
                    }
                }
            }
        }
        List<String> sorted = new ArrayList<String>( roles );
        Collections.sort( sorted );
        return sorted;
    }
}
