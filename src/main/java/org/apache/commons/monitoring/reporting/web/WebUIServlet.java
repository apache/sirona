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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.monitoring.reporting.Renderer;

/**
 * Extends the reporting servlet to return a nice HTML based web UI with
 * JavaScript enhancements.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class WebUIServlet
    extends MonitoringServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.web.MonitoringServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        String pathInfo = request.getPathInfo();
        if ( pathInfo.startsWith( "/resources" ) )
        {
            String path = pathInfo.substring( "/resources".length() );
            InputStream resource = getClass().getResourceAsStream( path );
            if ( resource != null )
            {
                response.setContentType( getMimeTypes( pathInfo ) );
                response.addHeader( "Cache-Control", "max-age=1000" );
                copy( resource, response.getOutputStream() );
            }
            return;
        }
        super.service( request, response );
    }

    /**
     * @param pathInfo
     * @return
     */
    protected String getMimeTypes( String pathInfo )
    {
        if ( pathInfo.endsWith( ".css" ) )
        {
            return "text/css";
        }
        if ( pathInfo.endsWith( ".js" ) )
        {
            return "text/javascript";
        }
        if ( pathInfo.endsWith( ".gif" ) )
        {
            return "image/gif";
        }
        return null;
    }

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    private void copy( InputStream input, OutputStream output )
        throws IOException
    {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
        try
        {
            input.close();
        }
        catch ( IOException e )
        {
            // ignore...
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.web.MonitoringServlet#getRenderer(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected Renderer getRenderer( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        List<String> accept = getAcceptedMimeTypes( request );
        if ( accept.contains( "text/html" ) )
        {
            return new NiceHtmlRenderer( request.getContextPath() + request.getServletPath() );
        }
        return super.getRenderer( request, response );
    }
}
