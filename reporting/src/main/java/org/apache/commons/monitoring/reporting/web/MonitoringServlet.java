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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.reporting.Format;
import org.apache.commons.monitoring.reporting.FormattingVisitor;

public class MonitoringServlet
    extends HttpServlet
{
    private Repository repository;

    @Override
    public void init( ServletConfig config )
        throws ServletException
    {
        repository = (Repository) config.getServletContext().getAttribute( Repository.class.getName() );
    }

    private static Map<String, Format> formats = new HashMap<String, Format>();
    static
    {
        formats.put( "application/json", Format.JSON );
        formats.put( "text/javascript", Format.JSON );
        formats.put( "application/xml", Format.XML );
        formats.put( "text/xml", Format.XML );
    }

    private static Map<String, Format> extensions = new HashMap<String, Format>();
    static
    {
        extensions.put( "json", Format.JSON );
        extensions.put( "js", Format.JSON );
        extensions.put( "xml", Format.XML );
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        String mime = HttpUtils.parseAccept( req.getHeader( "Accept" ) );
        Format format = formats.get( mime );
           
        if ( format == null )
        {
            String path = req.getRequestURI();
            String extension = path.substring( path.lastIndexOf( '.' ) );
            format = formats.get( extension );
        }
        
        Visitor visitor = new FormattingVisitor( format, resp.getWriter() );
        repository.accept( visitor );
    }
}
