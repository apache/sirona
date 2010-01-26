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

package org.apache.commons.monitoring.reporting.jaxrs;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.reporting.Format;
import org.apache.commons.monitoring.reporting.FormattingVisitor;
import org.apache.commons.monitoring.reporting.web.HttpUtils;

@Path( "/repository" )
public class RepositoryResource
{
    private Repository repository;

    @GET
    @Produces( TEXT_XML )
    public String asXML( @Context ServletContext context )
    {
        this.repository = (Repository) context.getAttribute( Repository.class.getName() );
        return asFormat( Format.XML );
    }

    @GET
    @Produces( APPLICATION_JSON )
    @Path( "/monitoring" )
    public String asJSONP( @Context ServletContext context,
                           @QueryParam( "callback" ) String callback )
    {
        this.repository = (Repository) context.getAttribute( Repository.class.getName() );
        String json = asFormat( Format.JSON );
        if ( callback != null )
        {
            return callback + "(" + json + ")";
        }
        return json;
    }

    public String asFormat( Format format )
    {
        PrintWriter writer = new PrintWriter( new StringWriter() );
        Visitor visitor = new FormattingVisitor( format, writer );
        repository.accept( visitor );
        return writer.toString();
    }
}
