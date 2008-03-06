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

package org.apache.commons.monitoring;


/**
 * A <code>StatValue</code> is a numerical indicator of some monitored
 * application state with support for simple statistics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface StatValue
{

    /**
     * @return the value
     */
    long get();

    /**
     * @param l the value to set
     */
    void set( long l, Unit unit );

    /**
     * reset the statValue
     */
    void reset();

    /**
     * @return the minimum value
     */
    long getMin();

    /**
     * @return the maximum value
     */
    long getMax();

    /**
     * @return the arithmetic mean value
     */
    double getMean();

    /**
     * Compute the standard deviation : measures the dispersion of values around
     * the average value = sqrt( variance ).
     *
     * @return the value standard deviation
     */
    double getStandardDeviation();

    /**
     * Set the monitor this value is attached to
     * @param monitor
     */
    void setMonitor( Monitor monitor );

    /**
     * @return the monitor this value is attached to
     */
    Monitor getMonitor();

    /**
     * @return the role for this value in the monitor
     */
    String getRole();

    /**
     * @return the (primary) data unit
     */
    Unit getUnit();

    void addListener( Listener listener );

    void removeListener( Listener listener );

    /**
     * Listener for StatValue events
     */
    public static interface Listener
    {
        void onValueChanged( StatValue value, long l );
    }
}
