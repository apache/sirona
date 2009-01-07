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

package org.apache.commons.monitoring.reporting;

import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;

/**
 * An enum to acces data from a Metric based on the property name. Can be used to avoid reflection on Metric
 * implementation when requesting data and undesirable exposure of internals.
 * <p>
 * example :
 * 
 * <pre>
 * String property = httpServletRequest.getParameter( &quot;property&quot; );
 * 
 * Double data = MetricData.valueOf( property ).value( metric );
 * </pre>
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public enum MetricData
{
    Hits
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getHits();
        }
    },
    Max
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getMax();
        }
    },
    Mean
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getMean();
        }
    },
    Min
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getMin();
        }
    },
    StandardDeviation
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getStandardDeviation();
        }
    },
    Sum
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getSum();
        }
    },
    SumOfLogs
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getSumOfLogs();
        }
    },
    SumOfSquares
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getSumOfSquares();
        }
    },
    Variance
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getVariance();
        }
    },
    GeometricMean
    {
        @Override
        public double value( Metric metric )
        {
            return metric.getGeometricMean();
        }
    },
    Value
    {
        @Override
        public double value( Metric metric )
        {
            if ( metric instanceof Gauge )
            {
                return ( (Gauge) metric ).getValue();
            }
            return metric.getSum();
        }
    };
    public abstract double value( Metric metric );
}