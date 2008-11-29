package org.apache.commons.monitoring.monitors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.metrics.NoOpCounter;
import org.apache.commons.monitoring.metrics.NoOpGauge;

/**
 * Monitor implementation that does nothing, and return NoOp metrics when requested. Designed for test purpose or to
 * disable monitoring to compare monitored to unmonitored performances.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
@SuppressWarnings( "unchecked" )
public class NullMonitor
    implements Monitor
{
    private static Role NOOP = new Role( "NoOp", Unit.UNARY, Metric.class );

    private static NoOpCounter counter = new NoOpCounter( NOOP );

    private static NoOpGauge gauge = new NoOpGauge( NOOP );

    private Collection<Metric<?>> metrics = Arrays.asList( new Metric<?>[] { counter, gauge } );

    public Counter getCounter( String role )
    {
        return counter;
    }

    public Counter getCounter( Role<Counter> role )
    {
        return counter;
    }

    public Gauge getGauge( String role )
    {
        return gauge;
    }

    public Gauge getGauge( Role<Gauge> role )
    {
        return gauge;
    }

    public Key getKey()
    {
        return new Key( "noOp", null, null );
    }

    public Metric getMetric( String role )
    {
        return counter;
    }

    public <M extends Metric<?>> M getMetric( Role<M> role )
    {
        return (M) counter;
    }

    public Collection<Metric<?>> getMetrics()
    {
        return metrics;
    }

    public Collection<Role> getRoles()
    {
        return Collections.singletonList( NOOP );
    }

    public void reset()
    {
        // NoOp
    }

}
