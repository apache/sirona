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

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Metric.Observable;

/**
 * Listener for a Metric that will invoke {@link ThresholdListener#exceed(Observable, double, double)} when a value set
 * to the metric exceed the threshold. The threshold value may be dynamic.
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ThresholdListener
    implements Metric.Listener
{
    public abstract double getThreshold();

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.Metric.Listener#onValueChanged(org.apache.commons.monitoring.Metric.Observable,
     *      double)
     */
    public void onValueChanged( Observable metric, double value )
    {
        double threshold = getThreshold();
        if ( value > threshold )
        {
            exceed( metric, threshold, value );
        }

    }

    public abstract void exceed( Observable metric, double threshold, double value );
}
