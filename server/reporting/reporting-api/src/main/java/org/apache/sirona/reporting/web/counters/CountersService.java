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
package org.apache.sirona.reporting.web.counters;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 0.3
 */
@Path( "/counters" )
public class CountersService
{

    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public List<CounterInfo> all( @QueryParam( "unit" ) String unitName )
    {
        Collection<Counter> counters = Repository.INSTANCE.counters();

        List<CounterInfo> out = new ArrayList<CounterInfo>( counters.size() );

        Unit unit = null;

        if ( unitName != null )
        {
            unit = Unit.get( unitName );
        }

        for ( Counter counter : counters )
        {
            Unit currentUnit = counter.getKey().getRole().getUnit();
            if ( unit == null )
            {
                out.add( new CounterInfo( new KeyInfo( counter.getKey() ), //
                                          MetricData.Hits.value( counter ), //
                                          MetricData.Max.value( counter ), //
                                          MetricData.Mean.value( counter ), //
                                          MetricData.Min.value( counter ), //
                                          MetricData.StandardDeviation.value( counter ), //
                                          MetricData.Sum.value( counter ), //
                                          MetricData.Variance.value( counter ), //
                                          MetricData.Concurrency.value( counter ), //
                                          MetricData.MaxConcurrency.value( counter ) ) ); //
            }
            else
            {
                out.add( new CounterInfo( new KeyInfo( counter.getKey() ).unitName( unit.getName() ), //
                                          MetricData.Hits.value( counter ), //
                                          unit.convert( MetricData.Max.value( counter ), currentUnit ), //
                                          unit.convert( MetricData.Mean.value( counter ), currentUnit ), //
                                          unit.convert( MetricData.Min.value( counter ), currentUnit ), //
                                          unit.convert( MetricData.StandardDeviation.value( counter ), currentUnit ), //
                                          unit.convert( MetricData.Sum.value( counter ), currentUnit ), //
                                          unit.convert( MetricData.Variance.value( counter ), currentUnit ), //
                                          MetricData.Concurrency.value( counter ), //
                                          MetricData.MaxConcurrency.value( counter ) ) ); //
            }
        }

        return out;

    }


    @GET
    @Path( "/{name}/{unitName}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public CounterInfo find( @PathParam( "name" ) String name, @PathParam( "unitName" ) String unit )
    {
        final Counter counter =
            Repository.INSTANCE.getCounter( new Counter.Key( new Role( name, Unit.get( unit ) ), name ) );

        return counter == null
            ? null
            : new CounterInfo( new KeyInfo( counter.getKey() ), MetricData.Hits.value( counter ), //
                               MetricData.Max.value( counter ), MetricData.Mean.value( counter ), //
                               MetricData.Min.value( counter ), MetricData.StandardDeviation.value( counter ), //
                               MetricData.Sum.value( counter ), MetricData.Variance.value( counter ), //
                               MetricData.Concurrency.value( counter ), MetricData.MaxConcurrency.value( counter ) );

    }


}
