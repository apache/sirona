package org.apache.commons.monitoring.metrics;

import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

public class NoOpGauge
    extends AbstractMetric
    implements Gauge
{
    public NoOpGauge( Role role )
    {
        super( role );
    }

    public Metric.Type getType()
    {
        return Metric.Type.GAUGE;
    }

    public void reset()
    {
        // NoOp
    }

    public void decrement( Unit unit )
    {
        // NoOp
    }

    public double getValue()
    {
        return 0;
    }

    public void increment( Unit unit )
    {
        // NoOp
    }

    public void set( double value, Unit unit )
    {
        // NoOp
    }

}
