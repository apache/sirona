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

import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.ValidationResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @since 0.3
 */
public class NodeStatusInfo
    implements Serializable
{

    private final String name;

    private final List<ValidationResultInfo> results;

    private final Date date;

    private final String status;

    private final String statusLabel;

    public NodeStatusInfo( String name, NodeStatus nodeStatus )
    {
        this.name = name;
        this.date = nodeStatus.getDate();
        this.status = nodeStatus.getStatus().toString();
        this.statusLabel = StatusHelper.map( nodeStatus.getStatus() );

        if ( nodeStatus.getResults() != null )
        {
            this.results = new ArrayList<ValidationResultInfo>( nodeStatus.getResults().length );
            for ( ValidationResult validationResult : nodeStatus.getResults() )
            {
                this.results.add( new ValidationResultInfo( validationResult.getStatus().toString(),//
                                                            StatusHelper.map( validationResult.getStatus() ), //
                                                            validationResult.getMessage(), //
                                                            validationResult.getName() ) );
            }
        }
        else
        {
            this.results = new ArrayList<ValidationResultInfo>( 0 );
        }
    }

    public List<ValidationResultInfo> getResults()
    {
        return results;
    }

    public Date getDate()
    {
        return date;
    }

    public String getStatus()
    {
        return status;
    }

    public String getStatusLabel()
    {
        return statusLabel;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "NodeStatusInfo{" +
            "name='" + name + '\'' +
            ", results=" + results +
            ", date=" + date +
            ", status='" + status + '\'' +
            '}';
    }
}
