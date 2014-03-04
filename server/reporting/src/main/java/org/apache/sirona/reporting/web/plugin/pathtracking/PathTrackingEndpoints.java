package org.apache.sirona.reporting.web.plugin.pathtracking;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.reporting.web.plugin.api.graph.Graphs;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.util.Environment;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

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
        return new Template( "pathtracking/home.vm", params );
    }

    @Regex( "/startend/([0-9]*)/([0-9]*)" )
    public String startend( final long start, final long end )
    {

        Collection<String> entries = PATH_TRACKING_DATA_STORE.retrieveTrackingIds( new Date( start ), new Date( end ) );

        MapBuilder<String, String> mapBuilder = new MapBuilder<String, String>();

        for ( String entry : entries )
        {
            mapBuilder = mapBuilder.set( "trackingId", entry );
        }

        return toJson( mapBuilder.build() );

    }



    static String toJson(final Map<String, String> data) { // helper to generate Json
        final StringBuilder builder = new StringBuilder().append("[");
        final Iterator<Map.Entry<String,String>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, String> entry = iterator.next();
            builder.append("[") //
                .append(entry.getKey().toString()) //
                .append(", ") //
                .append(entry.getValue().toString()) //
                .append("]");
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.append("]").toString();
    }

}
