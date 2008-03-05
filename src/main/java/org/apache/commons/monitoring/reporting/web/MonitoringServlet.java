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
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.reporting.HtmlRenderer;
import org.apache.commons.monitoring.reporting.JsonRenderer;
import org.apache.commons.monitoring.reporting.Renderer;
import org.apache.commons.monitoring.reporting.Selector;
import org.apache.commons.monitoring.reporting.XmlRenderer;
import org.apache.commons.monitoring.reporting.Renderer.Filter;

/**
 * A reporting servlet to format repository datas in HttpResponse, according to
 * flexible rules :
 * <ul>
 * <li> The output format is discovered based on request HTTP <tt>accept</tt>
 * header, from a set of registered Renderers</li>
 * <li> The request path is used as a <tt>Selector</tt> to retrieve a
 * collection of monitors from the repository</li>
 * <li> The request parameters are used to build a <tt>Renderer.Filter</tt></li>
 * </ul>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    /** key for Repository as ServletContext attribute */
    private static final String REPOSITORY_KEY = Repository.class.getName();

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
        renderers.put( "text/javascript", new JsonRenderer() );
        renderers.put( "application/json", new JsonRenderer() );
        renderers.put( "text/xml", new XmlRenderer() );
        renderers.put( "text/html", new HtmlRenderer() );

        Object attribute = getServletContext().getAttribute( REPOSITORY_KEY );
        if ( attribute != null )
        {
            if ( attribute instanceof Repository )
            {
                this.repository = (Repository) attribute;
            }
            else
            {
                throw new ServletException( "Attribute " + REPOSITORY_KEY + " in servletContext is not a Repository" );
            }
        }
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
    @SuppressWarnings("unchecked")
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        Renderer renderer = getRenderer( request, response );
        if ( renderer != null )
        {
            String path = request.getPathInfo();
            Collection<Monitor> monitors = (Collection<Monitor>) new Selector( path ).select( repository );
            Renderer.Filter filter = getFilter( request );
            renderer.render( response.getWriter(), monitors, filter );
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
    protected Filter getFilter( final HttpServletRequest request )
    {
        return new Filter()
        {
            private List<String> roles;

            private List<String> categories;

            private List<String> subsystems;
            {
                roles = Arrays.asList( request.getParameterValues( "role" ) );
                categories = Arrays.asList( request.getParameterValues( "category" ) );
                subsystems = Arrays.asList( request.getParameterValues( "subsystem" ) );
            }

            public boolean render( Object object )
            {
                if ( object instanceof StatValue )
                {
                    return roles.isEmpty() || roles.contains( ( (StatValue) object ).getRole() );
                }
                if ( object instanceof Monitor )
                {
                    Key key = ( (Monitor) object ).getKey();
                    return ( categories.isEmpty() || categories.contains( key.getCategory() ) )
                        && ( subsystems.isEmpty() || subsystems.contains( key.getSubsystem() ) );
                }
                return true;
            }

        };
    }

    protected List<String> getAcceptedMimeTypes( HttpServletRequest request )
    {
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
        return mimeTypes;
    }

}
