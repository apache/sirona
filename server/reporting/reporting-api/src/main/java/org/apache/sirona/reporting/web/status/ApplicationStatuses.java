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

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Olivier Lamy
 * @since 0.3
 */
public class ApplicationStatuses
    implements Serializable
{

    private final String name;

    private final Collection<NodeStatusInfo> nodeStatusInfos;

    public ApplicationStatuses( String name, Collection<NodeStatusInfo> nodeStatusInfos )
    {
        this.name = name;
        this.nodeStatusInfos = nodeStatusInfos;
    }

    public String getName()
    {
        return name;
    }

    public Collection<NodeStatusInfo> getNodeStatusInfos()
    {
        return nodeStatusInfos;
    }

    @Override
    public String toString()
    {
        return "ApplicationStatuses{" +
            "name='" + name + '\'' +
            ", nodeStatusInfo=" + nodeStatusInfos +
            '}';
    }
}
