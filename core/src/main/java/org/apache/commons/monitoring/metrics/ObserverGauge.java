package org.apache.commons.monitoring.metrics;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Unit;

/**
 * A Gauge that observe another Gauge and computes stats until it gets detached
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ObserverGauge
    extends ObserverMetric<Gauge>
    implements Gauge
{
    private Gauge.Observable observable;

    private ThreadSafeGauge delegate;

    public ObserverGauge( Gauge.Observable observable )
    {
        super( observable.getRole() );
        this.observable = observable;
        this.delegate = new RentrantLockGauge( getRole() );
        delegate.threadSafeSet( observable.getValue() );
        observable.addListener( this );
    }

    @Override
    protected SummaryStatistics getSummary()
    {
        return delegate.getSummary();
    }

    @Override
    protected Metric.Observable<Gauge> getObservable()
    {
        return observable;
    }

    public void onValueChanged( Metric.Observable<Gauge> metric, double value )
    {
        delegate.threadSafeSet( value );
    }

    public double getValue()
    {
        return delegate.getValue();
    }

    public void increment( Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public void set( double value, Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public void decrement( Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public Metric.Type getType()
    {
        return Metric.Type.GAUGE;
    }

    public void reset()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }
}
