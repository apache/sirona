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

package org.apache.sirona.reporting.web.sessions;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.Graph;
import org.apache.sirona.repositories.Repositories;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.web.session.SessionGauge;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.3
 */
@Path( "/sessions" )
public class SessionsReports
{

    @GET
    @Path( "/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public List<Graph> sessions( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {

        List<Graph> graphs = new ArrayList<Graph>();

        for ( final Role gauge : Repositories.findByPrefixAndUnit( SessionGauge.SESSIONS_PREFIX, Unit.UNARY ) )
        {

            graphs.add( new Graph( "Sessions-" + gauge.getName(), Graph.DEFAULT_COLOR, //
                                   Repository.INSTANCE.getGaugeValues( start, end, gauge ) ) );
        }

        return graphs;
    }

}
