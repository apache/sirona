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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.listeners.Detachable;

/**
 * Render as a graph, based on jQuery Flot javascript library
 * <p>
 * This renderer produces JavaScript, and expect the HTML page to include
 * <ul>
 * <li> the required &lt;script&gt; tags :
 *
 * <pre>
 *   &lt;script src=&quot;jquery.pack.js&quot; &gt;&lt;/script&gt;
 *   &lt;script src=&quot;jquery.flot.pack.js&quot; &gt;&lt;/script&gt;
 *   &lt;!--[if IE]&gt;&lt;script src=&quot;excanvas.pack.js&quot; &gt;&lt;/script&gt;&lt;![endif]--&gt;
 * </pre>
 *
 * </li>
 * <li> a &lt;div& id="placeholder"gt; to draw the graph into, having
 * <tt>width</tt> and <tt>height</tt> style set. </li>
 * </ul>
 *
 * @see http://code.google.com/p/flot/
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class FlotRenderer
    extends AbstractRenderer
{
    private static final String[] ATTRIBUTES = { "hits", "sum", "min", "max", "mean", "deviation", "value" };

    public FlotRenderer()
    {
        super( "text/javascript" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.Renderer#render(org.apache.commons.monitoring.reporting.Context,
     * java.util.Collection,
     * org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    public void render( final Context ctx, Collection<Monitor> monitors, Options options )
    {
        prepareRendering( ctx, monitors, options );
        ctx.print( "$.plot($('#placeholder'), [" );

        int color = 0;
        for ( Iterator<String> it = ( (Collection) ctx.get( ROLES ) ).iterator(); it.hasNext(); )
        {
            String role = it.next();
            List<String> attribues = new ArrayList<String>();
            for ( String attribute : ATTRIBUTES )
            {
                if ( !options.render( role, attribute ) )
                {
                    continue;
                }
                attribues.add( attribute );
            }

            for ( Iterator<String> attr = attribues.iterator(); attr.hasNext(); )
            {
                final String attribute = attr.next();
                ctx.print( "{ color: " + color );
                ctx.print( ", label: \"" );
                ctx.print( role + "." + attribute );
                ctx.print( "\", data: " );
                ctx.print( "[" );
                int x = 0;
                int rendered = 0;
                for ( final Iterator<Monitor> iterator = monitors.iterator(); iterator.hasNext(); )
                {
                    Monitor monitor = iterator.next();
                    x++;
                    StatValue value = monitor.getValue( role );
                    if ( value == null )
                    {
                        continue;
                    }
                    ctx.put( "x", x );
                    if ( rendered > 0 )
                    {
                        ctx.print( "," );
                    }
                    ctx.put( "rendered", false );
                    render( ctx, value, new OptionsSupport()
                    {
                        @Override
                        public boolean render( String role, String string )
                        {
                            boolean render = string.equals( attribute );
                            if ( render )
                            {
                                ctx.put( "rendered", true );
                            }
                            return render;
                        }
                    } );
                    if ( ( (Boolean) ctx.get( "rendered" ) ).booleanValue() )
                    {
                        rendered++;
                    }
                }
                ctx.print( "]" );
                ctx.print( "}" );
                if ( attr.hasNext() )
                {
                    ctx.print( "," );
                }
                color++;
            }
            if ( it.hasNext() )
            {
                ctx.print( "," );
            }
        }

        ctx.print( "] );" );
    }

    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        ctx.print( "[" + ctx.get( "x" ) + "," );
        super.render( ctx, value, attribute, number, options, ratio );
        ctx.print( "]" );
    }

    @Override
    protected void renderNaN( Context ctx )
    {
        ctx.print( "NaN" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(org.apache.commons.monitoring.reporting.Context,
     * org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    protected void render( Context ctx, Key key )
    {
        // Not used here
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#renderDetached(org.apache.commons.monitoring.reporting.Context,
     * org.apache.commons.monitoring.listeners.Detachable,
     * org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    protected void renderDetached( Context ctx, Detachable detached, Options options )
    {
        // Not used here
    }

}
