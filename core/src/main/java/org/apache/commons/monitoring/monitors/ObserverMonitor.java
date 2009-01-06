package org.apache.commons.monitoring.monitors;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Detachable;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.metrics.ObserverCounter;
import org.apache.commons.monitoring.metrics.ObserverGauge;

public class ObserverMonitor
    extends AbstractMonitor
    implements Monitor.Listener, Detachable
{
    /**
     * The monitor beeing observed. All event/data gathered by this monitor will be duplicated here
     */
    private Monitor.Observable observable;

    private boolean detached;

    private long attachedAt;

    private long detachedAt;

    public ObserverMonitor( Monitor.Observable observable )
    {
        super( observable.getKey() );
        this.observable = observable;
        this.attachedAt = System.currentTimeMillis();
        this.detached = false;
        this.observable.addListener( this );
        for ( Metric metric : observable.getMetrics() )
        {
            onMetricRegistered( observable, metric );
        }
    }

    @SuppressWarnings( "unchecked" )
    public void onMetricRegistered( Monitor.Observable monitor, Metric metric )
    {
        if ( metric instanceof Counter.Observable )
        {
            register( new ObserverCounter( (Counter.Observable) metric ) );
        }
        else if ( metric instanceof Gauge.Observable )
        {
            register( new ObserverGauge( (Gauge.Observable) metric ) );
        }
    }

    @SuppressWarnings( "unchecked" )
    public void detach()
    {
        detached = true;
        for ( Metric metric : observable.getMetrics() )
        {
            if ( metric instanceof Metric.Observable )
            {
                Metric.Observable observableMetric = (Metric.Observable) metric;
                Metric.Listener observer = (Metric.Listener) getMetric( metric.getRole() );
                observableMetric.removeListener( observer );
            }
        }
        detachedAt = System.currentTimeMillis();
    }

    public boolean isDetached()
    {
        return detached;
    }

    public long getAttachedAt()
    {
        return attachedAt;
    }

    public long getDetachedAt()
    {
        return detachedAt;
    }

}
