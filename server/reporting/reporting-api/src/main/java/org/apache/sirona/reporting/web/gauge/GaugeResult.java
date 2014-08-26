package org.apache.sirona.reporting.web.gauge;

import java.io.Serializable;
import java.util.List;

/**
 * @author Olivier Lamy
 */
public class GaugeResult
    implements Serializable
{

    private final String gaugeName;

    private final List<GaugeValue> gaugeValues;

    public GaugeResult( String gaugeName, List<GaugeValue> gaugeValues )
    {
        this.gaugeName = gaugeName;
        this.gaugeValues = gaugeValues;
    }

    public String getGaugeName()
    {
        return gaugeName;
    }

    public List<GaugeValue> getGaugeValues()
    {
        return gaugeValues;
    }

    @Override
    public String toString()
    {
        return "GaugeResult{" +
            "gaugeName='" + gaugeName + '\'' +
            ", gaugeValues=" + gaugeValues +
            '}';
    }
}
