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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.reporting.Context;
import org.apache.commons.monitoring.reporting.FlotRenderer;
import org.apache.commons.monitoring.reporting.HtmlRenderer;
import org.apache.commons.monitoring.reporting.JsonRenderer;
import org.apache.commons.monitoring.reporting.OptionsSupport;
import org.apache.commons.monitoring.reporting.Renderer;
import org.apache.commons.monitoring.reporting.Selector;
import org.apache.commons.monitoring.reporting.XmlRenderer;
import org.apache.commons.monitoring.reporting.Renderer.Options;
import org.apache.commons.monitoring.servlet.ServletContextUtil;

/**
 * A reporting servlet to format repository datas in HttpResponse, according to
 * flexible rules :
 * <ul>
 * <li> The output format is discovered based on request HTTP <tt>accept</tt>
 * header, from a set of registered Renderers</li>
 * <li> The request path is used as a <tt>Selector</tt> to retrieve a
 * collection of monitors from the repository</li>
 * <li> The request parameters are used to build a <tt>Renderer.Options</tt></li>
 * </ul>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private Map<String, Renderer> renderers = new ConcurrentHashMap<String, Renderer>();

    private Repository repository;

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
        throws ServletException
    {
        renderers.put( "text/javascript", new JsonRenderer( "text/javascript" ) );
        renderers.put( "text/flot", new FlotRenderer() );
        renderers.put( "application/json", new JsonRenderer( "application/json" ) );
        renderers.put( "text/xml", new XmlRenderer() );
        renderers.put( "text/html", new HtmlRenderer() );

        repository = ServletContextUtil.getRepository( getServletContext() );
    }

    public void registerRenderer( String mimeType, Renderer renderer )
    {
        renderers.put( mimeType, renderer );
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        Renderer renderer = getRenderer( request, response );
        if ( renderer != null )
        {
            String path = request.getPathInfo();
            Collection<Monitor> monitors = (Collection<Monitor>) new Selector( path ).select( repository );
            Renderer.Options options = getOptions( request );
            Context ctx = new Context( response.getWriter() );
            renderer.render( ctx, monitors, options );
            return;
        }
    }

    protected Renderer getRenderer( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        List<String> accept = getAcceptedMimeTypes( request );
        for ( String mimeType : accept )
        {
            Renderer renderer = renderers.get( mimeType );
            if ( renderer != null )
            {
                response.setContentType( mimeType );
                return renderer;
            }
        }
        response.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
        return null;
    }

    /**
     * @param request
     * @return
     */
    protected Options getOptions( final HttpServletRequest request )
    {
        return new HttpSerlvetRequestOptions( request );
    }

    protected List<String> getAcceptedMimeTypes( HttpServletRequest request )
    {
        String mime = request.getParameter( "format" );
        if ( mime != null )
        {
            if ( mime.indexOf( '/' ) < 0 )
            {
                mime = "text/" + mime;
            }
            return Collections.singletonList( mime );
        }
        List<String> mimeTypes = new ArrayList<String>();
        String accept = request.getHeader( "accept" );
        if ( accept.length() != 0 )
        {
            StringTokenizer tokenizer = new StringTokenizer( accept, "," );
            while ( tokenizer.hasMoreTokens() )
            {
                mimeTypes.add( tokenizer.nextToken() );
            }
        }
        if ( accept.contains( "*/*" ) || accept.contains( "text/html" ) )
        {
            // Let IE and FireFox get HTML !
            mimeTypes.add( 0, "text/html" );
        }
        return mimeTypes;
    }

    protected class HttpSerlvetRequestOptions
        extends OptionsSupport
    {
        protected final HttpServletRequest request;

        protected List<String> roles;

        protected List<String> categories;

        protected List<String> subsystems;

        /**
         * @param request
         */
        public HttpSerlvetRequestOptions( HttpServletRequest request )
        {
            this.request = request;
            String[] values = request.getParameterValues( "role" );
            if (values != null)
            {
                roles = Arrays.asList( values );
            }
            values = request.getParameterValues( "category" );
            categories = values != null ? Arrays.asList( values ) : Collections.<String> emptyList();
            values = request.getParameterValues( "subsystem" );
            subsystems = values != null ? Arrays.asList( values ) : Collections.<String> emptyList();
        }

        @Override
        public boolean renderRole( Role role )
        {
            return roles != null ? roles.contains( role.getName() ) : true;
        }

        @Override
        public boolean render( Monitor monitor )
        {
            Key key = monitor.getKey();
            return ( categories.isEmpty() || categories.contains( key.getCategory() ) )
                && ( subsystems.isEmpty() || subsystems.contains( key.getSubsystem() ) );
        }

        @Override
        public boolean render( Role role, String attribute )
        {
            String columns = request.getParameter( role.getName() + ".columns" );
            if ( columns == null )
            {
                return true;
            }
            return columns.indexOf( attribute ) >= 0;
        }

        @Override
        public Unit unitFor( Role role )
        {
            String unitName = request.getParameter( role.getName() + ".unit" );
            if ( unitName != null )
            {
                if ( role.getUnit() != null )
                {
                    Unit unit = role.getUnit().getDerived( unitName );
                    if ( unit != null )
                    {
                        return unit;
                    }
                }
            }
            return role.getUnit();
        }
    }

}
