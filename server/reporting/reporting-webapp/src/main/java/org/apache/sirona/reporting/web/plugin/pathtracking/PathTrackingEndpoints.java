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
package org.apache.sirona.reporting.web.plugin.pathtracking;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.reporting.web.plugin.report.format.HTMLFormat;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.util.Environment;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class PathTrackingEndpoints
{

    private static final PathTrackingDataStore PATH_TRACKING_DATA_STORE =
        IoCs.findOrCreateInstance( PathTrackingDataStore.class );

    @Regex
    public Template home()
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        if ( !Environment.isCollector() )
        {

        }

        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DATE, -1 );

        Collection<PathCallInformation> pathCallInformations =
            PATH_TRACKING_DATA_STORE.retrieveTrackingIds( cal.getTime(), new Date() );

        params.put( "pathCallInformations", pathCallInformations );

        return new Template( "pathtracking/home.vm", params );
    }

    @Regex( "/startend/([0-9]*)/([0-9]*)" )
    public String startend( final long start, final long end )
    {

        Collection<PathCallInformation> entries =
            PATH_TRACKING_DATA_STORE.retrieveTrackingIds( new Date( start ), new Date( end ) );

        MapBuilder<String, String> mapBuilder = new MapBuilder<String, String>();

        for ( PathCallInformation entry : entries )
        {
            mapBuilder = mapBuilder.set( //
                                         new MapBuilder<String, String>() //
                                             .set( "trackingId", entry.getTrackingId() ) //
                                             .set( "startTime", Long.toString( entry.getStartTime().getTime() ) ) //
                                             .build()
            );
        }

        return toJson( mapBuilder.build() );

    }

    /**
     * retrieve a slice of pathtracking entries
     *
     * @param pathTrackingId
     * @param number
     * @return
     */
    @Regex( "/pathtrackingdetail/(.*)/(.*)" )
    public Template displayPathTrackingDetail( String pathTrackingId, String number )
    {

        Collection<PathTrackingEntry> entries =
            PATH_TRACKING_DATA_STORE.retrieve( pathTrackingId, Integer.parseInt( number ) );

        return new Template( "pathtracking/pathtrackingdetail.vm", //
                             new MapBuilder<String, Object>() //
                                 .set( "headers", HTMLFormat.ATTRIBUTES_ORDERED_LIST ) //
                                 .set( "entries", entries ).build()//
        );
    }


    static String toJson( final Map<String, String> data )
    { // helper to generate Json
        final StringBuilder builder = new StringBuilder().append( "[" );
        final Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();
        while ( iterator.hasNext() )
        {
            final Map.Entry<String, String> entry = iterator.next();
            builder.append( "[" ) //
                .append( entry.getKey().toString() ) //
                .append( ", " ) //
                .append( entry.getValue().toString() ) //
                .append( "]" );
            if ( iterator.hasNext() )
            {
                builder.append( ", " );
            }
        }
        return builder.append( "]" ).toString();
    }

}
