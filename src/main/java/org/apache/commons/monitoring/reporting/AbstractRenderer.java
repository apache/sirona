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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.reporting.Renderer.Options;

/**
 * Render a collection of monitor for reporting
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRenderer
    implements Renderer
{
    public final void render( PrintWriter writer, Collection<Monitor> monitors )
    {
        render( writer, monitors, new OptionsSupport() );
    }

    public void render( PrintWriter writer, Collection<Monitor> monitors, Options options )
    {
        int count = 0;
        for ( Monitor monitor : monitors )
        {
            if ( options.render( monitor ) )
            {
                if (count > 0)
                {
                    hasNext( writer, Monitor.class );
                }
                render( writer, monitor, options );
                count ++;
            }
        }
    }

    protected void hasNext( PrintWriter writer, Class<?> type )
    {
        // Nop
    }

    protected void render( PrintWriter writer, Monitor monitor, Options options )
    {
        render( writer, monitor.getKey() );
        renderStatValues( writer, monitor, options );
    }

    protected int renderStatValues( PrintWriter writer, Monitor monitor, Options options )
    {

        // Sort values by role to ensure predictable result
        List<StatValue> values = getOrderedStatValues( monitor, options );
        for ( Iterator<StatValue> iterator = values.iterator(); iterator.hasNext(); )
        {
            StatValue value = (StatValue) iterator.next();
            render( writer, value, options );
            if (iterator.hasNext())
            {
                hasNext( writer, StatValue.class );
            }
        }
        return values.size();
    }

    protected List<StatValue> getOrderedStatValues( Monitor monitor, Options options )
    {
        List<StatValue> values = new LinkedList<StatValue>( monitor.getValues() );
        for ( Iterator<StatValue> iterator = values.iterator(); iterator.hasNext(); )
        {
            StatValue value = (StatValue) iterator.next();
            if ( ! options.render( value ))
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
        });
        return values;
    }

    protected void render( PrintWriter writer, StatValue value, Options options )
    {
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            if ( options.render( value, "hits" ) )
            {
                render( writer, value, "hits", counter.getHits(), options );
            }
            if ( options.render( value, "sum" ) )
            {
                render( writer, value, "sum", counter.getSum(), options );
            }
        }
        if ( options.render( value, "min" ) )
        {
            render( writer, value, "min", value.getMin(), options );
        }
        if ( options.render( value, "max" ) )
        {
            render( writer, value, "max", value.getMax(), options );
        }
        if ( options.render( value, "mean" ) )
        {
            render( writer, value, "mean", value.getMean(), options );
        }
        if ( options.render( value, "deviation" ) )
        {
            render( writer, value, "deviation", value.getStandardDeviation(), options );
        }
        if ( options.render( value, "value" ) )
        {
            render( writer, value, "value", value.get(), options );
        }
    }

    protected abstract void render( PrintWriter writer, Key key );


    protected void render( PrintWriter writer, StatValue value, String attribute, Number number, Options options )
    {
        if ( number instanceof Double )
        {
            renderInternal( writer, value, attribute, number.doubleValue(), options );
        }
        else
        {
            renderInternal( writer, value, attribute, number.longValue(), options );
        }
    }

    private void renderInternal( PrintWriter writer, StatValue value, String attribute, long l, Options options )
    {
        Unit unit = options.unitFor( value );
        if (unit != null)
        {
            l = l / unit.getScale();
        }
        writer.append( NumberFormat.getInstance( options.getLocale() ).format( l ) );
    }

    private void renderInternal( PrintWriter writer, StatValue value, String attribute, double d, Options options )
    {
        if (Double.isNaN( d ))
        {
            writer.append( "-" );
            return;
        }
        Unit unit = options.unitFor( value );
        if (unit != null)
        {
            d = d / unit.getScale();
        }
        writer.append( DecimalFormat.getNumberInstance( options.getLocale() ).format( d ) );
    }
}
