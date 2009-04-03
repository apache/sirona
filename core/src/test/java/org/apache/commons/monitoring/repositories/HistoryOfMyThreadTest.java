package org.apache.commons.monitoring.repositories;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.stopwatches.HistoryOfMyThread;

/**
 * @author ndeloof
 *
 */
public class HistoryOfMyThreadTest
    extends TestCase
    implements HistoryOfMyThread.Listener
{
    private HistoryOfMyThread historyOfMyThread;

    public void testHistoryEnd()
        throws Exception
    {
        HOMTRepositoryDecorator repository = new HOMTRepositoryDecorator();
        repository.decorate( new DefaultRepository() );
        repository.addListener( this );

        StopWatch s1 = repository.start( repository.getMonitor( "test0" ) );
        StopWatch s2 = repository.start( repository.getMonitor( "test1" ) );
        s2.stop();
        StopWatch s3 = repository.start( repository.getMonitor( "test2" ) );
        s3.stop();
        assertNull( historyOfMyThread );
        s1.stop();

        assertNotNull( historyOfMyThread );
        List<StopWatch> history = historyOfMyThread.history();
        assertEquals( 3, history.size() );
        assertEquals( s1, history.get( 0 ) );
        assertEquals( s2, history.get( 1 ) );
        assertEquals( s3, history.get( 2 ) );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.stopwatches.HistoryOfMyThread.Listener#onHistoryEnd(org.apache.commons.monitoring.stopwatches.HistoryOfMyThread,
     *      long)
     */
    public void onHistoryEnd( HistoryOfMyThread history, long elapsedTime )
    {
        historyOfMyThread = history;
    }
}
