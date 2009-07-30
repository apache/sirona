package org.apache.commons.monitoring.metrics;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;

public class NoOpCounter
    extends AbstractMetric
    implements Counter
{
    public NoOpCounter( Role role )
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

    public void add( double delta )
    {
        // NoOp
    }
}
