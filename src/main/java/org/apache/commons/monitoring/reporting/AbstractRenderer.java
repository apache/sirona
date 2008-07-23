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
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.listeners.Detachable;

/**
 * Render a collection of monitor for reporting
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRenderer
    implements Renderer
{
    private String contentType;


    protected static final String MONITORS = "monitors";

    protected static final String ROLES = "roles";

    public AbstractRenderer( String contentType )
    {
        super();
        this.contentType = contentType;
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
        ctx.put( ROLES, getRoles( monitors, options ) );
        ctx.put( MONITORS, monitors );
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
        if ( isDetatched( monitor ) )
        {
            renderDetached( ctx, (Detachable) monitor, options );
        }
        render( ctx, monitor.getKey() );
        renderMetrics( ctx, monitor, options );
    }

    protected boolean isDetatched( Monitor monitor )
    {
        return monitor instanceof Detachable && ( (Detachable) monitor ).isDetached();
    }

    protected abstract void renderDetached( Context ctx, Detachable detached, Options options );

    @SuppressWarnings( "unchecked" )
    protected void renderMetrics( Context ctx, Monitor monitor, Options options )
    {
        List<Role> roles = (List<Role>) ctx.get( ROLES );
        renderMetrics( ctx, monitor, options, roles );
    }

    @SuppressWarnings("unchecked")
    protected void renderMetrics( Context ctx, Monitor monitor, Options options, List<Role> roles )
    {
        for ( Iterator<Role> iterator = roles.iterator(); iterator.hasNext(); )
        {
            Role role = iterator.next();
            Metric metric = monitor.getMetric( role );
            if ( metric != null )
            {
                render( ctx, metric, options );
            }
            else
            {
                renderMissingMetric( ctx, role );
            }
            if ( iterator.hasNext() )
            {
                hasNext( ctx, Metric.class );
            }
        }
    }

    /**
     * Render an expected metric not supported by the current monitor
     * 
     * @param ctx
     * @param role
     */
    @SuppressWarnings("unchecked")
    protected void renderMissingMetric( Context ctx, Role role )
    {
        // Nop
    }

    protected List<Metric> getOrderedMetrics( Monitor monitor, Options options )
    {
        List<Metric> metrics = new LinkedList<Metric>( monitor.getMetrics() );
        for ( Iterator<Metric> iterator = metrics.iterator(); iterator.hasNext(); )
        {
            Metric value = (Metric) iterator.next();
            if ( !options.renderRole( value.getRole() ) )
            {
                iterator.remove();
            }
        }
        Collections.sort( metrics, new Comparator<Metric>()
        {
            public int compare( Metric m1, Metric m2 )
            {
                return m1.getRole().compareTo( m2.getRole() );
            }
        } );
        return metrics;
    }

    @SuppressWarnings("unchecked")
    protected void render( Context ctx, Metric metric, Options options )
    {
        Role role = metric.getRole();
        if ( metric instanceof Counter )
        {
            Counter counter = (Counter) metric;
            if ( options.render( role, "hits" ) )
            {
                render( ctx, metric, "hits", counter.getHits(), options, 0 );
            }
            if ( options.render( role, "sum" ) )
            {
                render( ctx, metric, "sum", counter.getSum(), options );
            }
            if ( options.render( role, "squares" ) )
            {
                render( ctx, metric, "squares", counter.getSumOfSquares(), options );
            }
        }
        if ( options.render( role, "min" ) )
        {
            render( ctx, metric, "min", metric.getMin(), options );
        }
        if ( options.render( role, "max" ) )
        {
            render( ctx, metric, "max", metric.getMax(), options );
        }
        if ( options.render( role, "mean" ) )
        {
            render( ctx, metric, "mean", metric.getMean(), options );
        }
        if ( options.render( role, "deviation" ) )
        {
            render( ctx, metric, "deviation", metric.getStandardDeviation(), options, 1 );
        }
        if ( options.render( role, "value" ) )
        {
            render( ctx, metric, "value", metric.get(), options, 1 );
        }
    }

    protected abstract void render( Context ctx, Key key );

    protected void render( Context ctx, Metric metric, String attribute, Number number, Options options )
    {
        render( ctx, metric, attribute, number, options, 1 );
    }

    /**
     * Render a Metric attribute
     * 
     * @param ctx output
     * @param metric the Metric that hold data to be rendered
     * @param attribute the Metric attribute name to be rendered
     * @param number the the Metric attribute value to be rendered
     * @param ratio the ratio between attribute unit and statValue unit (in power of 10)
     * @param options the rendering options
     */
    protected void render( Context ctx, Metric metric, String attribute, Number number, Options options, int ratio )
    {
        if ( number instanceof Double )
        {
            renderInternal( ctx, metric, attribute, number.doubleValue(), options, ratio );
        }
        else
        {
            renderInternal( ctx, metric, attribute, number.longValue(), options, ratio );
        }
    }

    private void renderInternal( Context ctx, Metric metric, String attribute, long l, Options options, int ratio )
    {
        Unit unit = options.unitFor( metric.getRole() );
        if ( unit != null )
        {
            while ( ratio-- > 0 )
            {
                l = l / unit.getScale();
            }
        }

        ctx.print( options.getNumberFormat().format( l ) );
    }

    private void renderInternal( Context ctx, Metric metric, String attribute, double d, Options options, int ratio )
    {
        if ( Double.isNaN( d ) )
        {
            renderNaN( ctx );
            return;
        }
        Unit unit = options.unitFor( metric.getRole() );
        if ( unit != null )
        {
            while ( ratio-- > 0 )
            {
                d = d / unit.getScale();
            }
        }

        ctx.print( options.getDecimalFormat().format( d ) );
    }

    protected void renderNaN( Context ctx )
    {
        ctx.print( "-" );
    }

    /**
     * @param monitors
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<Role> getRoles( Collection<Monitor> monitors, Options options )
    {
        Set<Role> roles = new HashSet<Role>();
        for ( Monitor monitor : monitors )
        {
            if ( options.render( monitor ) )
            {
                for ( Role role : monitor.getRoles() )
                {
                    if ( options.renderRole( role ) )
                    {
                        roles.add( role );
                    }
                }
            }
        }
        List<Role> sorted = new ArrayList<Role>( roles );
        Collections.<Role>sort( sorted );
        return sorted;
    }

    public String getContentType()
    {
        return contentType;
    }
}
