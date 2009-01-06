package org.apache.commons.monitoring.metrics;

import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

/**
 * * Thread-safe implementation of <code>Gauge</code>, based on synchronized methods.
 * <p>
 * Maintains a sum of (value * time) on each gauge increment/decrement operation to compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ThreadSafeGauge
    extends ObservableMetric<Gauge>
    implements Gauge, Gauge.Observable
{
    protected double value;

    protected long lastUse;

    protected double firstUse = Double.NaN;

    protected Min min = new Min();

    protected Max max = new Max();

    public ThreadSafeGauge( Role<Gauge> role )
    {
        super( role );
    }

    public double getValue()
    {
        return value;
    }

    public void increment( Unit unit )
    {
        add( 1, unit );
    }

    public void decrement( Unit unit )
    {
        add( -1, unit );
    }

    protected void add( double delta, Unit unit )
    {
        delta = normalize( delta, unit );
        double d = threadSafeAdd( delta );
        fireValueChanged( d );
    }

    protected double threadSafeAdd( double delta )
    {
        threadSafeSet( value + delta );
        return value;
    }

    protected long nanotime()
    {
        return System.nanoTime();
    }

    public double get()
    {
        return value;
    }

    public void set( double d, Unit unit )
    {
        d = normalize( d, unit );
        threadSafeSet( d );
        fireValueChanged( d );
    }

    protected abstract void threadSafeSet( double d );

    protected void doReset()
    {
        // Don't reset value !
        getSummary().clear();
        lastUse = 0;
        firstUse = Double.NaN;
    }

    protected void doThreadSafeSet( double d )
    {
        value = d;
        long now = nanotime();
        if ( Double.isNaN( firstUse ) )
        {
            firstUse = now;
        }
        else
        {
            long delta = now - lastUse;
            double s = d * delta;
            getSummary().addValue( s );
        }
        lastUse = now;
        min.increment( d );
        max.increment( d );
    }

    @Override
    public double getMean()
    {
        if ( Double.isNaN( lastUse ) || Double.isNaN( firstUse ) )
        {
            return Double.NaN;
        }
        return super.getMean() / ( lastUse - firstUse );
    }

}