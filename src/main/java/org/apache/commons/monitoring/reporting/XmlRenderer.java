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
import java.util.List;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.listeners.Detachable;

public class XmlRenderer
    extends AbstractRenderer
{

    public XmlRenderer()
    {
        super( "text/xml" );
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.Context, java.util.Collection, org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    public void render( Context ctx, Collection<Monitor> monitors, Options options )
    {
        ctx.print( "<monitors>" );
        super.render( ctx, monitors, options );
        ctx.print( "</monitors>" );
    }

    @Override
    public void render( Context ctx, Monitor monitor, Options options, List<String> roles )
    {
        render( ctx, monitor, options );
    }

    @Override
    public void render( Context ctx, Monitor monitor, Options options )
    {
        ctx.print( "<monitor " );
        super.render( ctx, monitor, options );
        ctx.print( "</monitor>" );
    }

    @Override
    public void render( Context ctx, Key key )
    {
        ctx.print( "name=\"" );
        ctx.print( key.getName() );
        if ( key.getCategory() != null )
        {
            ctx.print( "\" category=\"" );
            ctx.print( key.getCategory() );
        }
        if ( key.getSubsystem() != null )
        {
            ctx.print( "\" subsystem=\"" );
            ctx.print( key.getSubsystem() );
        }
        ctx.print( "\">" );
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#renderDetached(org.apache.commons.monitoring.reporting.Context, org.apache.commons.monitoring.listeners.SecondaryMonitor, org.apache.commons.monitoring.reporting.Renderer.Options)
     */
    @Override
    protected void renderDetached( Context ctx, Detachable detached, Options options )
    {
        ctx.print( "<period from=\"" );
        ctx.println ( options.getDateFormat().format( new Date( detached.getAttachedAt()) ) );
        ctx.print( "\" to=\"" );
        ctx.print( options.getDateFormat().format( new Date( detached.getDetachedAt()) ) );
        ctx.print( "/>" );
    }



    @Override
    public void render( Context ctx, StatValue value, Options options )
    {
        ctx.print( "<" );
        ctx.print( value.getRole() );
        super.render( ctx, value, options );
        ctx.print( "/>" );
    }

    @Override
    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        ctx.print( " " );
        ctx.print( attribute );
        ctx.print( "=\"" );
        super.render( ctx, value, attribute, number, options, ratio );
        ctx.print( "\"" );
    }

}
