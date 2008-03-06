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

package org.apache.commons.monitoring.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;

/**
* A servlet Options to be configured in your WEB-INF/web.xml to intercept all
 * incoming request.
 *
 * <pre>
 *         &lt;filter&gt;
 *         &lt;filter-name&gt;Monitoring&lt;/filter-name&gt;
 *         &lt;filter-class&gt;org.apache.commons.monitoring.servlet.MonitoringFilter&lt;/filter-class&gt;
 *         &lt;/filter&gt;
 *         &lt;filter-mapping&gt;
 *         &lt;filter-name&gt;Monitoring&lt;/filter-name&gt;
 *         &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *         &lt;/filter-mapping&gt;
 * </pre>
 *
 * <p>
 * A monitor will be created for each application URI. The requested resource extension
 * is used to determine the category.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringFilter
    implements Filter
{

    private Repository repository;

    private Map<String, String> categories = new ConcurrentHashMap<String, String>();

    public void registerCategoryForExtension( String extension, String category )
    {
        categories.put( extension, category );
    }

    /**
     * Delegates to Http based doFilter. {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        if ( request instanceof HttpServletRequest )
        {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            doFilter( httpRequest, httpResponse, chain );
        }
        else
        {
            // Not an HTTP request...
            chain.doFilter( request, response );
        }
    }

    public void doFilter( HttpServletRequest request, HttpServletResponse response,
                          FilterChain chain )
        throws IOException, ServletException
    {
        String uri = getRequestedUri( request );
        String category = getCategory( uri );
        Monitor monitor = repository.getMonitor( uri, category );

        StopWatch stopWatch = repository.start( monitor );
        try
        {
            chain.doFilter( request, response );
        }
        finally
        {
            stopWatch.stop();
        }
    }

    /**
     * @param request
     * @return
     */
    protected String getRequestedUri( HttpServletRequest request )
    {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        uri = uri.substring( context.length() );
        return uri;
    }

    /**
     * @param uri
     * @return
     */
    protected String getCategory( String uri )
    {
        int i = uri.lastIndexOf( "." );
        if ( i > 0 )
        {
            return categories.get( uri.substring( i ) );
        }
        return categories.get( "other" );
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
        throws ServletException
    {
        repository = ServletContextUtil.getRepository( config.getServletContext() );
        registerCategoryForExtension( ".do", "struts" );
        registerCategoryForExtension( ".action", "struts2" );
        registerCategoryForExtension( ".jsf", "jsf" );
        registerCategoryForExtension( ".js", "static" );
        registerCategoryForExtension( ".html", "static" );
        registerCategoryForExtension( ".css", "static" );
        registerCategoryForExtension( ".gif", "static" );
        registerCategoryForExtension( ".png", "static" );
        registerCategoryForExtension( ".jpg", "static" );
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        // Nop
    }

}
