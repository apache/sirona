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

package org.apache.commons.monitoring.metrics;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

/**
 * A simple implementation of {@link Metric}. Only provide methods to compute stats from aggregated data provided by
 * derived classes. Requires the derived classes to provide support for thread-safety.
 * <p>
 * Design note : Use {@link SummaryStatistics} to compute statistics on monitored datas.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractMetric
    implements Metric
{
    private Monitor monitor;

    private SummaryStatistics statistics;

    private Role role;

    private Unit unit;

    public AbstractMetric( Role role )
    {
        super();
        if ( role.getType() != this.getType() )
        {
            throw new IllegalArgumentException( "Invalid Role type for " + getClass().getName() );
        }
        this.role = role;
        this.unit = role.getUnit();
        this.statistics = new SummaryStatistics();
    }

    protected SummaryStatistics getSummary()
    {
        // TODO should we clone for thread safety ?
        return statistics;
    }

    protected double normalize( double value, Unit unit )
    {
        if ( !this.unit.isCompatible( unit ) )
        {
            throw new IllegalArgumentException( "role " + role + " is incompatible with unit " + unit );
        }
        return value * unit.getScale() / this.unit.getScale();
    }

    public Monitor getMonitor()
    {
        return monitor;
    }

    public Role getRole()
    {
        return role;
    }

    public void setMonitor( Monitor monitor )
    {
        if ( this.monitor != null && this.monitor != monitor )
        {
            throw new IllegalStateException( "value is allready attached to a monitor" );
        }
        this.monitor = monitor;
    }

    public Unit getUnit()
    {
        return unit;
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getMax()
     */
    public double getMax()
    {
        return getSummary().getMax();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getMin()
     */
    public double getMin()
    {
        return getSummary().getMin();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getN()
     */
    public long getHits()
    {
        return getSummary().getN();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getStandardDeviation()
     */
    public double getStandardDeviation()
    {
        return getSummary().getStandardDeviation();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getSum()
     */
    public double getSum()
    {
        return getSummary().getSum();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.StatisticalSummary#getVariance()
     */
    public double getVariance()
    {
        return getSummary().getVariance();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.SummaryStatistics#getMean()
     */
    public double getMean()
    {
        return getSummary().getMean();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.SummaryStatistics#getGeometricMean()
     */
    public double getGeometricMean()
    {
        return getSummary().getGeometricMean();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.SummaryStatistics#getSumOfLogs()
     */
    public double getSumOfLogs()
    {
        return getSummary().getSumOfLogs();
    }

    /**
     * @return
     * @see org.apache.commons.math.stat.descriptive.SummaryStatistics#getSumsq()
     */
    public double getSumOfSquares()
    {
        return getSummary().getSumsq();
    }
}
