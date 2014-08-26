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
package org.apache.sirona.reporting.web.gauge;

import org.apache.commons.codec.binary.Base64;
import org.apache.sirona.Role;
import org.apache.sirona.repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @since 0.3
 */
@Path( "/gauges" )
public class GaugeService
{
    private static final String UTF8 = "UTF-8";

    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Collection<GaugeInfo> all()
    {
        return sortNames( Repository.INSTANCE.gauges() );
    }


    @GET
    @Path( "/{gaugeName}/{start}/{end}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public GaugeResult find( @PathParam( "gaugeName" ) String encodedGaugeName, //
                             @PathParam( "start" ) final long start, //
                             @PathParam( "end" ) final long end )
    {

        String gaugeName = decode( encodedGaugeName );

        Role role = Repository.INSTANCE.findGaugeRole( gaugeName );
        if ( role == null )
        {
            return null;
        }

        SortedMap<Long, Double> values = Repository.INSTANCE.getGaugeValues( start, end, role );

        List<GaugeValue> gaugeValues = new ArrayList<GaugeValue>( values.size() );

        for ( Map.Entry<Long, Double> entry : values.entrySet() )
        {
            gaugeValues.add( new GaugeValue( entry.getKey(), entry.getValue() ) );
        }

        return new GaugeResult( gaugeName, gaugeValues );

    }

    private static Collection<GaugeInfo> sortNames( final Collection<Role> gauges )
    {
        final Map<String, String> names = new TreeMap<String, String>();
        for ( final Role gauge : gauges )
        {
            final String name = gauge.getName();
            names.put( name, encode( name ) );
        }

        Collection<GaugeInfo> out = new ArrayList<GaugeInfo>( names.size() );

        for ( Map.Entry<String, String> entry : names.entrySet() )
        {
            out.add( new GaugeInfo( entry.getKey(), entry.getValue() ) );
        }

        return out;
    }

    private static String encode( final String role )
    {
        final String base64 = Base64.encodeBase64URLSafeString( role.getBytes() );
        try
        {
            return URLEncoder.encode( base64, UTF8 );
        }
        catch ( final UnsupportedEncodingException e )
        {
            return role; // shouldn't occur
        }
    }

    private static String decode( final String base64Role )
    {
        try
        {
            return new String( Base64.decodeBase64( URLDecoder.decode( base64Role, UTF8 ) ) );
        }
        catch ( final UnsupportedEncodingException e )
        {
            return base64Role; // shouldn't occur
        }
    }

}
