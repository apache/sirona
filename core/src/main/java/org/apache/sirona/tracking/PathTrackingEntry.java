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
package org.apache.sirona.tracking;

/**
 * @author Olivier Lamy
 */
public class PathTrackingEntry
{

    /**
     * a generated id to follow up the path call
     */
    private String trackingId;

    /**
     * the current entry id
     */
    private String pathTrakingEntryId;

    /**
     * the parent entry id
     */
    private String parentPathTrakingEntryId;

    /**
     * server instance for this call
     */
    private String nodeId;

    /**
     * name of the used class
     */
    private String className;

    /**
     * used method name
     */
    private String methodName;

    /**
     * start time for the method call in nano seconds
     */
    private long startTime;

    /**
     * time to execute the method in nano seconds
     */
    private long executionTime;

    public PathTrackingEntry( String trackingId, String nodeId, String className, String methodName, long startTime,
                              long executionTime )
    {
        this.trackingId = trackingId;
        this.nodeId = nodeId;
        this.className = className;
        this.methodName = methodName;
        this.startTime = startTime;
        this.executionTime = executionTime;
    }

    public String getTrackingId()
    {
        return trackingId;
    }

    public void setTrackingId( String trackingId )
    {
        this.trackingId = trackingId;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId( String nodeId )
    {
        this.nodeId = nodeId;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName( String className )
    {
        this.className = className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName( String methodName )
    {
        this.methodName = methodName;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime( long startTime )
    {
        this.startTime = startTime;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public void setExecutionTime( long executionTime )
    {
        this.executionTime = executionTime;
    }

    public String getPathTrakingEntryId()
    {
        return pathTrakingEntryId;
    }

    public void setPathTrakingEntryId( String pathTrakingEntryId )
    {
        this.pathTrakingEntryId = pathTrakingEntryId;
    }

    public String getParentPathTrakingEntryId()
    {
        return parentPathTrakingEntryId;
    }

    public void setParentPathTrakingEntryId( String parentPathTrakingEntryId )
    {
        this.parentPathTrakingEntryId = parentPathTrakingEntryId;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder( "PathTrackingEntry{" );
        sb.append( "trackingId='" ).append( trackingId ).append( '\'' );
        sb.append( ", pathTrakingEntryId='" ).append( pathTrakingEntryId ).append( '\'' );
        sb.append( ", parentPathTrakingEntryId='" ).append( parentPathTrakingEntryId ).append( '\'' );
        sb.append( ", nodeId='" ).append( nodeId ).append( '\'' );
        sb.append( ", className='" ).append( className ).append( '\'' );
        sb.append( ", methodName='" ).append( methodName ).append( '\'' );
        sb.append( ", startTime=" ).append( startTime );
        sb.append( ", executionTime=" ).append( executionTime );
        sb.append( '}' );
        return sb.toString();
    }
}
