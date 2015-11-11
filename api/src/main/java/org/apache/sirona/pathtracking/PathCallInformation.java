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
import java.util.Comparator;
import java.util.Date;

/**
 *
 */
public class PathCallInformation
    implements Serializable
{

    // use a default value here
    private static final long serialVersionUID = 4L;

    private String trackingId;

    /**
     * start time for the path in in nano seconds
     */
    private Date startTime;

    public PathCallInformation( String trackingId, Date startTime )
    {
        this.trackingId = trackingId;
        this.startTime = startTime;
    }

    public String getTrackingId()
    {
        return trackingId;
    }

    public void setTrackingId( String trackingId )
    {
        this.trackingId = trackingId;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime( Date startTime )
    {
        this.startTime = startTime;
    }

    @Override
    public String toString()
    {
        return "PathCallInformation{" +
            "trackingId='" + trackingId + '\'' +
            ", startTime=" + startTime +
            '}';
    }

    /**
     * start time comparaison
     */
    public static final Comparator<PathCallInformation> COMPARATOR = new Comparator<PathCallInformation>()
    {
        @Override
        public int compare( PathCallInformation o1, PathCallInformation o2 )
        {
            final int i = o1.getStartTime().compareTo(o2.getStartTime());
            if (i == 0)
            {
                return o1.getTrackingId().compareTo(o2.getTrackingId());
            }
            return i;
        }
    };
}
