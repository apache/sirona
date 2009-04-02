package org.apache.commons.monitoring.repositories;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.monitoring.StopWatch;

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
        s1.stop();

        assertNotNull( historyOfMyThread );
        Iterator<StopWatch> history = historyOfMyThread.history();
        assertEquals( s1, history.next() );
        assertEquals( s2, history.next() );
        assertEquals( s3, history.next() );
        assertFalse( history.hasNext() );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.repositories.HistoryOfMyThread.Listener#onHistoryEnd(org.apache.commons.monitoring.repositories.HistoryOfMyThread)
     */
    public void onHistoryEnd( HistoryOfMyThread history )
    {
        historyOfMyThread = history;
    }
}
