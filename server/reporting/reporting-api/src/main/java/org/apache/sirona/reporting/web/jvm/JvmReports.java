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
package org.apache.sirona.reporting.web.jvm;

import org.apache.sirona.gauges.jvm.ActiveThreadGauge;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.gauges.jvm.UsedNonHeapMemoryGauge;
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
@Path( "/jvmreports" )
public class JvmReports
{

    @GET
    @Path( "/cpu/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph cpu( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues( start, end, CPUGauge.CPU );
        return new Graph( "CPU Usage", Graph.DEFAULT_COLOR, gaugeValues );

    }

    @GET
    @Path( "/memory/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph memory( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues =
            Repository.INSTANCE.getGaugeValues( start, end, UsedMemoryGauge.USED_MEMORY );
        return new Graph( "Used Memory", Graph.DEFAULT_COLOR, gaugeValues );

    }

    @GET
    @Path( "/nonheapmemory/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph nonHeapmemory( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues =
            Repository.INSTANCE.getGaugeValues( start, end, UsedNonHeapMemoryGauge.USED_NONHEAPMEMORY );
        return new Graph( "Used Non Heap Memory", Graph.DEFAULT_COLOR, gaugeValues );

    }

    @GET
    @Path( "/activethreads/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Graph activeThreads( @PathParam( "start" ) final long start, @PathParam( "end" ) final long end )
    {
        final SortedMap<Long, Double> gaugeValues =
            Repository.INSTANCE.getGaugeValues( start, end, ActiveThreadGauge.ACTIVE_THREAD );
        return new Graph( "Active Thread Count", Graph.DEFAULT_COLOR, gaugeValues );

    }

}
