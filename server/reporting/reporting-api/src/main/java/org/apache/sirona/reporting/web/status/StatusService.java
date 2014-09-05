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
package org.apache.sirona.reporting.web.status;

import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @since 0.3
 */
@Path( "/status" )
public class StatusService
{

    private static final String DEFAULT_ROOT = "-";

    private static final String APP_DELIMITER = "#";

    // FIXME olamy: write documentation on that as it's not very clear!! what's going on here!!
    // it's simply a copy/paste and adaptation of the previous code

    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public List<ApplicationStatuses> all()
    {
        final Map<String, Map<String, NodeStatusInfo>> statusesByApp =
            new HashMap<String, Map<String, NodeStatusInfo>>();
        for ( final Map.Entry<String, NodeStatus> entry : Repository.INSTANCE.statuses().entrySet() )
        {
            final String key = entry.getKey();
            final String[] segments;
            if ( key.contains( APP_DELIMITER ) )
            {
                segments = key.split( APP_DELIMITER );
            }
            else
            {
                segments = new String[]{ DEFAULT_ROOT, key };
            }

            Map<String, NodeStatusInfo> statusesOfTheApp = statusesByApp.get( segments[0] );
            if ( statusesOfTheApp == null )
            {
                statusesOfTheApp = new TreeMap<String, NodeStatusInfo>();
                statusesByApp.put( segments[0], statusesOfTheApp );
            }
            statusesOfTheApp.put( segments[1], new NodeStatusInfo( segments[1], entry.getValue() ) );
        }

        List<ApplicationStatuses> applicationStatuseses = new ArrayList<ApplicationStatuses>( statusesByApp.size() );

        for ( Map.Entry<String, Map<String, NodeStatusInfo>> entry : statusesByApp.entrySet() )
        {
            applicationStatuseses.add( new ApplicationStatuses( entry.getKey(), entry.getValue().values() ) );
        }

        return applicationStatuseses;
    }


    @GET
    @Path( "/{node}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public NodeStatusInfo find( @PathParam( "node" ) String node )
    {
        NodeStatus nodeStatus = Repository.INSTANCE.statuses().get( node );

        return new NodeStatusInfo( node, nodeStatus );
    }

}
