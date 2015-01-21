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
package org.apache.sirona.store.tracking;


import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 */
public interface PathTrackingDataStore
{
    void store( PathTrackingEntry pathTrackingEntry );

    void store( Collection<PathTrackingEntry> pathTrackingEntries );

    void clearEntries();

    /**
     * <b>the result will be orderer by startTime</b>
     *
     * @param trackingId
     * @return {@link List} of {@link org.apache.sirona.pathtracking.PathTrackingEntry} related to a tracking id
     */
    Collection<PathTrackingEntry> retrieve( String trackingId );

    Collection<PathTrackingEntry> retrieve( String trackingId, int number );

    Collection<PathTrackingEntry> retrieve( String trackingId, String start, String end );

    /**
     * @param startTime
     * @param endTime
     * @return {@link org.apache.sirona.pathtracking.PathCallInformation} of all trackingIds available in the system between startTime and endTime
     */
    Collection<PathCallInformation> retrieveTrackingIds( Date startTime, Date endTime );



}
