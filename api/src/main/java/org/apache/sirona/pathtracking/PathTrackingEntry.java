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
package org.apache.sirona.pathtracking;

import java.io.Serializable;

/**
 *
 */
public class PathTrackingEntry
    implements Serializable
{

    // use a default value here
    private static final long serialVersionUID = 4L;

    /**
     * a generated id to follow up the path call
     */
    private String trackingId;

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

    /**
     * the tree level in the hierarchy
     */
    private int level;

    public PathTrackingEntry()
    {
        // no op
    }

    public PathTrackingEntry( String trackingId, String nodeId, String className, String methodName, //
                              long startTime, long executionTime, int level )
    {
        this.trackingId = trackingId;
        this.nodeId = nodeId;
        this.className = className;
        this.methodName = methodName;
        this.startTime = startTime;
        this.executionTime = executionTime;
        this.level = level;
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

    public int getLevel()
    {
        return level;
    }

    public void setLevel( int level )
    {
        this.level = level;
    }

    @Override
    public String toString()
    {
        return "PathTrackingEntry{" + "trackingId='" + trackingId + '\'' + ", nodeId='" + nodeId + '\''
            + ", className='" + className + '\'' + ", methodName='" + methodName + '\'' + ", startTime=" + startTime
            + ", executionTime=" + executionTime + ", level=" + level + '}';
    }
}
