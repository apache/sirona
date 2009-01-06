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
public abstract class ObservableMetric<M extends Metric>
    extends AbstractMetric<M>
    implements Metric.Observable<M>
{

    private List<Listener<M>> listeners;

    /**
     * Constructor
     */
    public ObservableMetric( Role<M> role )
    {
        super( role );
        this.listeners = new CopyOnWriteArrayList<Listener<M>>();
    }

    public void addListener( Listener<M> listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener<M> listener )
    {
        listeners.remove( listener );
    }

    protected void fireValueChanged( double d )
    {
        // Notify listeners
        for ( Listener<M> listener : listeners )
        {
            listener.onValueChanged( this, d );
        }
    }
}
