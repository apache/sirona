package org.apache.commons.monitoring.metrics;

import org.apache.commons.monitoring.Detachable;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;

public abstract class ObserverMetric
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

    /**
     * @see org.apache.commons.monitoring.Detachable#detach()
     */
    public void detach()
    {
        getObservable().removeListener( this );
        detached = true;
        detachedAt = System.currentTimeMillis();
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#getAttachedAt()
     */
    public long getAttachedAt()
    {
        return attachedAt;
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#getDetachedAt()
     */
    public long getDetachedAt()
    {
        return detachedAt;
    }

    /**
     * @see org.apache.commons.monitoring.Detachable#isDetached()
     */
    public boolean isDetached()
    {
        return detached;
    }

}
