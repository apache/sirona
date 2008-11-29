package org.apache.commons.monitoring.instrumentation.servlet;

import java.io.IOException;

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

public class MonitoringFilter
{

    private Repository repository;
    private String subsystem;
    private String category;

    public MonitoringFilter()
    {
        super();
    }

    /**
     * Delegates to Http based doFilter. {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
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

    public void doFilter( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        String uri = getRequestedUri( request );
        String category = getCategory( uri );
        Monitor monitor = repository.getMonitor( uri, category, subsystem );

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
        return category;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
        throws ServletException
    {
        subsystem = config.getInitParameter( "subsystem" );
        category = config.getInitParameter( "category" );
        repository = ServletContextUtil.getRepository( config.getServletContext() );
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        // Nop
    }

}