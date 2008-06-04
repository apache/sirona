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

package org.apache.commons.monitoring.reporting.web;

import java.util.Map;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.reporting.Context;
import org.apache.commons.monitoring.reporting.HtmlRenderer;

/**
 * Extends the HtmlRenderer to support CSS and JS inclusion in the rendered HTML.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class NiceHtmlRenderer
    extends HtmlRenderer
{

    private String title = "Monitoring";

    /** The web application contextPath */
    private String contextPath;

    /** The CSS styleSheet to apply for a nicer look that brute HTML */
    private String stylesheet = "commons-monitoring.css";

    /** The JavaScripts to include to support user interaction */
    private String[] scripts =
        new String[] { "jquery-1.2.6.pack.js",
                       "jquery.tablesorter.pack.js", "commons-monitoring.js" };

    public NiceHtmlRenderer( String contextPath )
    {
        super();
        this.contextPath = contextPath;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.HtmlRenderer#documentHead(java.io.Context)
     */
    @Override
    protected void documentHead( Context ctx )
    {
        ctx.println( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " );
        ctx.println( "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" );
        ctx.println( "<html>" );
        ctx.println( "<head>" );
        if ( title != null )
        {
            ctx.print( "<title>" );
            ctx.print( title );
            ctx.println( "</title>" );
        }
        if ( stylesheet != null )
        {
            ctx.print( "<link rel='stylesheet' type='text/css' href='" );
            ctx.print( contextPath );
            ctx.print( "/resources/" );
            ctx.print( stylesheet );
            ctx.println( "' />" );
        }
        if ( scripts != null )
        {
            for ( int i = 0; i < scripts.length; i++ )
            {
                ctx.print( "<script src='" );
                ctx.print( contextPath );
                ctx.print( "/resources/" );
                ctx.print( scripts[i] );
                ctx.println( "' ></script>" );
            }
        }
        ctx.println( "</head>" );
        ctx.println( "<body>" );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void renderMissingValue( Context ctx, Role role )
    {
        Map<String, Integer> columns = (Map<String, Integer>) ctx.get( COLUMNS );
        ctx.print( "<td class='not-applicable' colspan='" );
        ctx.print( String.valueOf( columns.get( role ) ) );
        ctx.print( "'>-</td>" );
    }

    @Override
    protected void renderUnit( Context writer, Unit unit )
    {
        writer.print( " <span class='unit'>(" );
        writer.print( unit.getName() );
        writer.print( ")</span>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.HtmlRenderer#tableStartTag(java.io.Context)
     */
    @Override
    protected void tableStartTag( Context writer )
    {
        writer.println( "<table border='1' id='monitoring' cellspacing='1'>" );
    }

    public void setStylesheet( String stylesheet )
    {
        this.stylesheet = stylesheet;
    }

    public void setScripts( String... scripts )
    {
        this.scripts = scripts;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public void setContextPath( String contextPath )
    {
        if ( this.contextPath == null )
        {
            this.contextPath = contextPath;
        }
    }
}
