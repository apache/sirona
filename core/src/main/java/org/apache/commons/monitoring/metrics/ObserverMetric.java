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

import org.apache.commons.monitoring.Detachable;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;

public abstract class ObserverMetric<T extends Metric>
    extends AbstractMetric
    implements Detachable, Metric.Listener
{
    protected abstract Metric.Observable getObservable();

    private boolean detached;

    private long attachedAt;

    private long detachedAt;

    public ObserverMetric( Role role )
    {
        super( role );
        attachedAt = System.currentTimeMillis();
    }

    protected abstract T getDelegate();

    public final long getHits()
    {
        return getDelegate().getHits();
    }

    public final double getMin()
    {
        return getDelegate().getMin();
    }

    public final double getMax()
    {
        return getDelegate().getMax();
    }

    public final double getMean()
    {
        return getDelegate().getMean();
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#detach()
     */
    public final void detach()
    {
        getObservable().removeListener( this );
        detached = true;
        detachedAt = System.currentTimeMillis();
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#getAttachedAt()
     */
    public final long getAttachedAt()
    {
        return attachedAt;
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#getDetachedAt()
     */
    public final long getDetachedAt()
    {
        return detachedAt;
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#isDetached()
     */
    public final boolean isDetached()
    {
        return detached;
    }

}
