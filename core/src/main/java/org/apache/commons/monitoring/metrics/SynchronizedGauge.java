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

import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;

/**
 * Thread-safe implementation of <code>Gauge</code>, based on synchronized
 * methods.
 * <p>
 * Maintains a sum of (value * time) on each gauge increment/decrement operation
 * to compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SynchronizedGauge
    extends ThreadSafeGauge
    implements Gauge
{
    public SynchronizedGauge( Role<Gauge> role )
    {
        super( role );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.Metric#getType()
     */
    public Type getType()
    {
        return Type.GAUGE;
    }

    public synchronized void reset()
    {
        doReset();
    }

    protected synchronized void threadSafeSet( double d )
    {
        doThreadSafeSet( d );
    }
}
