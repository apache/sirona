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

import java.io.PrintWriter;

import org.apache.commons.monitoring.Unit;
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
        new String[] { "jquery-1.2.3.pack.js", "jquery.tablesorter.pack.js", "commons-monitoring.js" };

    public NiceHtmlRenderer( String contextPath )
    {
        super();
        this.contextPath = contextPath;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.HtmlRenderer#documentHead(java.io.PrintWriter)
     */
    @Override
    protected void documentHead( PrintWriter writer )
    {
        writer.println( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " );
        writer.println( "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" );
        writer.println( "<html>" );
        writer.println( "<head>" );
        if ( title != null )
        {
            writer.print( "<title>" );
            writer.print( title );
            writer.println( "</title>" );
        }
        if ( stylesheet != null )
        {
            writer.print( "<link rel='stylesheet' type='text/css' href='" );
            writer.print( contextPath );
            writer.print( "/resources/" );
            writer.print( stylesheet );
            writer.println( "' />" );
        }
        if ( scripts != null )
        {
            for ( int i = 0; i < scripts.length; i++ )
            {
                writer.print( "<script src='" );
                writer.print( contextPath );
                writer.print( "/resources/" );
                writer.print( scripts[i] );
                writer.println( "' ></script>" );
            }
        }
        writer.println( "</head>" );
        writer.println( "<body>" );
    }

    @Override
    protected void renderUnit( PrintWriter writer, Unit unit )
    {
        writer.print( " <span class='unit'>(" );
        writer.print( unit.getName() );
        writer.print( ")</span>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.HtmlRenderer#tableStartTag(java.io.PrintWriter)
     */
    @Override
    protected void tableStartTag( PrintWriter writer )
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
