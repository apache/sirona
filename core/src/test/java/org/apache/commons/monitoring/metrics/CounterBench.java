package org.apache.commons.monitoring.metrics;
import static junit.framework.Assert.assertEquals;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.metrics.RentrantLockCounter;
import org.apache.commons.monitoring.metrics.SynchronizedCounter;
import org.junit.Test;

public class CounterBench implements Runnable
{
    private static final int THREADS = 5;

    private static final int LOOPS = 100;

    private Counter counter;

    private String mode;


    @Test
    public void synchronizedCounter() throws Exception
    {
        mode = "SynchronizedCounter";
        counter = new SynchronizedCounter( Monitor.FAILURES );
        runConcurrent();
    }

    @Test
    public void rentrantLockCounter() throws Exception
    {
        mode = "RentrantLockCounter";
        counter = new RentrantLockCounter( Monitor.FAILURES );
        runConcurrent();
    }


    private void runConcurrent()
        throws InterruptedException
    {
        long start = System.nanoTime();
        ExecutorService pool = Executors.newFixedThreadPool( THREADS );
        for (int i = 0 ; i < LOOPS; i++)
        {
            pool.submit( this );
        }
        pool.awaitTermination( 60, TimeUnit.SECONDS );
        assertEquals( LOOPS, counter.getSum() );
        System.out.printf( "%s : %,d ns/operation %n", mode, ( System.nanoTime() - start ) /LOOPS);

    }

    public void run()
    {
        counter.add( 1, Unit.UNARY );
    }
}


