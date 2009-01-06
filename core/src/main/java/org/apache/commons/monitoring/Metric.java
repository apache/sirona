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

import java.util.EventListener;


/**
 * A <code>Metric</code> is a numerical indicator of some monitored
 * application state with support for simple statistics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Metric
{
    public enum Type
    {
        COUNTER, GAUGE
    };

    Type getType();

    /**
     * reset the Metric
     */
    void reset();

    /**
     * Set the monitor this Metric is attached to
     *
     * @param monitor
     * @throws IllegalStateException if the Metric is allready attached to a monitor
     */
    void setMonitor( Monitor monitor ) throws IllegalStateException;

    /**
     * @return the monitor this Metric is attached to
     */
    Monitor getMonitor();

    /**
     * @return the role for this Metric in the monitor
     */
    Role getRole();

    /**
     * @return the data unit
     */
    Unit getUnit();

    /**
     * Listener for Metric events
     */
    public static interface Listener
        extends EventListener
    {
        /**
         * Value has changed on Metric.
         * <p>
         * Note that the value parameter has not the same content depending
         * on Metric variant: for a {@ Counter} it is the delta added to the counter
         * for a {@ Gauge} it is the new value of the gauge after beeing updated.
         * @param metric
         * @param value
         */
        void onValueChanged( Observable metric, double value );
    }

    /**
     * A metric that support the Observer pattern.
     */
    public interface Observable
        extends Metric
    {
        /**
         * @param listener listener to get registered
         */
        void addListener( Listener listener );

        /**
         * @param listener listener to get removed
         */
        void removeListener( Listener listener );
    }

    // --- Statistical indicators --------------------------------------------

    double getMax();

    double getMin();

    long getHits();

    double getSum();

    double getStandardDeviation();

    double getVariance();

    double getMean();

    double getGeometricMean();

    double getSumOfLogs();

    double getSumOfSquares();
}
