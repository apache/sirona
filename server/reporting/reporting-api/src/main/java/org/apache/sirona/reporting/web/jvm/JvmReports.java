package org.apache.sirona.reporting.web.jvm;

import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.reporting.web.Graph;
import org.apache.sirona.repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.SortedMap;

/**
 * @author Olivier Lamy
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

}
