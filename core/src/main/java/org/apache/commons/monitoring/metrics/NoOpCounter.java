package org.apache.commons.monitoring.metrics;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

public class NoOpCounter
    extends AbstractMetric<Counter>
    implements Counter
{
    public NoOpCounter( Role<Counter> role )
    {
        super( role );
    }

    public Metric.Type getType()
    {
        return Metric.Type.COUNTER;
    }

    public void reset()
    {
        // NoOp
    }

    public void add( long delta, Unit unit )
    {
        // NoOp
    }
}
