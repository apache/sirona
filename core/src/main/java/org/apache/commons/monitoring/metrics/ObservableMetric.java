package org.apache.commons.monitoring.metrics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Role;

/**
 * Implements <tt>Observale</tt> pattern on the Metrics
 * <p>
 * Use a CopyOnWriteArrayList to avoid synchronization
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
@SuppressWarnings("unchecked")
public abstract class ObservableMetric
    extends AbstractMetric
    implements Metric.Observable
{

    private List<Listener> listeners;

    /**
     * Constructor
     */
    public ObservableMetric( Role role )
    {
        super( role );
        this.listeners = new CopyOnWriteArrayList<Listener>();
    }

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }

    protected void fireValueChanged( double d )
    {
        // Notify listeners
        for ( Listener listener : listeners )
        {
            listener.onValueChanged( this, d );
        }
    }
}
