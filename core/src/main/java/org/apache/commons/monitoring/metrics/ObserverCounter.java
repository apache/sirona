package org.apache.commons.monitoring.metrics;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Metric;

/**
 * A Counter that observe another Counter and computes stats until it gets detached
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ObserverCounter
    extends ObserverMetric
implements Counter
{
    private Counter.Observable observable;

    private ThreadSafeCounter delegate;

    public ObserverCounter( Counter.Observable observable )
    {
        super( observable.getRole() );
        this.observable = observable;
        this.delegate = new RentrantLockCounter( getRole() );
        observable.addListener( this );
    }

    @Override
    protected SummaryStatistics getSummary()
    {
        return delegate.getSummary();
    }

    @Override
    protected Metric.Observable getObservable()
    {
        return observable;
    }

    public void onValueChanged( Metric.Observable metric, double value )
    {
        delegate.threadSafeAdd( value );
    }

    public void add( double delta )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public Metric.Type getType()
    {
        return Metric.Type.COUNTER;
    }

    public void reset()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }
}
