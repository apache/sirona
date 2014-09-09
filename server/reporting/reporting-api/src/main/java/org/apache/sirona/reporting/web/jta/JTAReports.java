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

package org.apache.sirona.reporting.web.jta;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.Graph;
import org.apache.sirona.repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.SortedMap;

/**
 * @since 0.3
 */
@Path( "/jtareports" )
public class JTAReports
{

    // copied to avoid classloading issue depending on the deployment, see org.apache.sirona.jta.JTAGauges
    private static final Role COMMITED = new Role( "jta-commited", Unit.UNARY );

    private static final Role ROLLBACKED = new Role( "jta-rollbacked", Unit.UNARY );

    private static final Role ACTIVE = new Role( "jta-active", Unit.UNARY );

    @GET
    @Path( "/commits/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph commit( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues( start, end, COMMITED );
        return new Graph( "Commits", Graph.DEFAULT_COLOR, gaugeValues );

    }

    @GET
    @Path( "/rollbacks/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph rollback( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues( start, end, ROLLBACKED );
        return new Graph( "Rollbacks", Graph.DEFAULT_COLOR, gaugeValues );

    }

    @GET
    @Path( "/actives/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph active( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues( start, end, ACTIVE );
        return new Graph( "Actives", Graph.DEFAULT_COLOR, gaugeValues );

    }

}
