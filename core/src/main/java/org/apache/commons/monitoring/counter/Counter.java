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

package org.apache.commons.monitoring.counter;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.monitors.Monitor;

/**
 * A <code>Metric</code> is a numerical indicator of some monitored application state with support for simple
 * statistics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Counter {
    /**
     * reset the Metric
     */
    void reset();

    /**
     * Add value to the metric. For Counters, the value is expected to be always positive.
     * <p/>
     * The delta MUST use the metric unit ({@link #getUnit()})
     *
     * @param delta value to be added
     */
    void add(double delta);

    /**
     * Add value to the metric with the specified Unit. For Counters, the value is expected to be always positive.
     *
     * @param delta value to be added
     * @param unit  the unit used for delta, MUST be compatible with the metric unit ({@link #getUnit()})
     */
    void add(double delta, Unit unit);

    /**
     * Set the monitor this Metric is attached to
     *
     * @param monitor
     */
    void setMonitor(Monitor monitor);

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
