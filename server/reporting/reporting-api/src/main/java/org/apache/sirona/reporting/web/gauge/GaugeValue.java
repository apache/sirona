package org.apache.sirona.reporting.web.gauge;

import java.io.Serializable;

/**
 * @author Olivier Lamy
 */
public class GaugeValue
    implements Serializable
{

    private final long timestamp;

    private final double value;

    public GaugeValue( long timestamp, double value )
    {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public double getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "GaugeValue{" +
            "timestamp=" + timestamp +
            ", value=" + value +
            '}';
    }
}
