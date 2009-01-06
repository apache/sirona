package org.apache.commons.monitoring.metrics;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

public abstract class ThreadSafeCounter
    extends ObservableMetric<Counter>
    implements Counter, Counter.Observable
{
    public ThreadSafeCounter( Role<Counter> role )
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
        return Type.COUNTER;
    }

    public void add( double delta, Unit unit )
    {
        delta = normalize( delta, unit );
        threadSafeAdd( delta );
        fireValueChanged( delta );
    }

    /**
     * Implementation of this method is responsible to ensure thread safety. It is
     * expected to delegate computing to {@ #doThreadSafeAdd(long)}
     * @param delta
     */
    protected abstract void threadSafeAdd( double delta );

    protected void doThreadSafeAdd( double delta )
    {
        getSummary().addValue( delta );
    }

    protected void doReset()
    {
        getSummary().clear();
    }

}