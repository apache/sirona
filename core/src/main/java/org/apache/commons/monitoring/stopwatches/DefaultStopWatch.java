package org.apache.commons.monitoring.stopwatches;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;

/**
 * Implementation of StopWatch that maintains a Gauge of concurrent threads accessing the monitored resource.
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultStopWatch extends SimpleStopWatch
    implements StopWatch
{

    /**
     * Constructor.
     * <p>
     * The monitor can be set to null to use the StopWatch without the monitoring infrastructure.
     * 
     * @param monitor the monitor associated with the process to be monitored
     */
    public DefaultStopWatch( Monitor monitor )
    {
        super( monitor );
        doStart();
    }

    protected void doStart()
    {
        monitor.getGauge( Monitor.CONCURRENCY ).increment( Unit.UNARY );
    }

    protected void doStop()
    {
        super.doStop();
        monitor.getGauge( Monitor.CONCURRENCY ).decrement( Unit.UNARY );
    }


    protected void doCancel()
    {
        monitor.getGauge( Monitor.CONCURRENCY ).decrement( Unit.UNARY );
    }

}