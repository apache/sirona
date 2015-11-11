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

import java.util.concurrent.TimeUnit;

/**
 * @since 0.2
 */
public class PathTrackingInformation
{
    private static final long OFFSET;
    static
    {
        // nanoTime is a clock solution, we need start date for final query so computing the local offset
        // to get back a ~ date
        OFFSET = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) - System.nanoTime();
    }

    private String className;

    private String methodName;

    private final long start;

    private int level;

    public PathTrackingInformation( String className, String methodName )
    {
        this.className = className;
        this.methodName = methodName;
        this.start = System.nanoTime();
    }

    public long getStartDateNs()
    {
        return OFFSET + start;
    }

    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public long getStart()
    {
        return start;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel( final int level )
    {
        this.level = level;
    }

    @Override
    public String toString()
    {
        return "PathTrackingInformation{" +
            "className='" + className + '\'' +
            ", methodName='" + methodName + '\'' +
            ", start=" + start +
            ", level=" + level +
            '}';
    }
}
