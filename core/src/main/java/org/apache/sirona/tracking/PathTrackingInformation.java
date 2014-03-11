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
 * @since 0.2
 */
public class PathTrackingInformation
{
    private String className;

    private String methodName;

    private PathTrackingInformation parent;

    private long start;

    private long end;

    private int level;

    public PathTrackingInformation( String className, String methodName )
    {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public PathTrackingInformation getParent()
    {
        return parent;
    }

    public void setParent( PathTrackingInformation parent )
    {
        this.parent = parent;
    }

    public void setStart( final long start )
    {
        this.start = start;
    }

    public long getStart()
    {
        return start;
    }

    public long getEnd()
    {
        return end;
    }

    public void setEnd( final long end )
    {
        this.end = end;
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
        return "PathTrackingInformation{className='" + className + "', methodName='" + methodName + "\', parent="
            + parent + '}';
    }
}
