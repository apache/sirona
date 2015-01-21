/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sirona.cube;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.store.tracking.BatchPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.util.SerializeUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@AutoSet
public class DisruptorPathTrackingDataStore
    extends BatchPathTrackingDataStore
    implements CollectorPathTrackingDataStore
{
    private static final Cube CUBE = IoCs.findOrCreateInstance( CubeBuilder.class ).build();

    private static boolean USE_SINGLE_STORE = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.singlestore", "false" ) );

    private RingBuffer<PathTrackingEntry> ringBuffer;

    private Disruptor<PathTrackingEntry> disruptor;

    private int ringBufferSize = 4096;

    private int numberOfConsumers = 4;

    @Created
    public void initialize()
    {
        ExecutorService exec = Executors.newCachedThreadPool();

        // FIXME make configurable: WaitStrategy

        disruptor = new Disruptor<PathTrackingEntry>( new EventFactory<PathTrackingEntry>()
        {
            @Override
            public PathTrackingEntry newInstance()
            {
                return new PathTrackingEntry();
            }
        }, ringBufferSize, exec, ProducerType.SINGLE, new BusySpinWaitStrategy()
        );

        for ( int i = 0; i < numberOfConsumers; i++ )
        {
            disruptor.handleEventsWith( new PathTrackingEntryEventHandler( i, numberOfConsumers ) );
        }
        ringBuffer = disruptor.start();

    }

    private static class PathTrackingEntryEventHandler
        implements EventHandler<PathTrackingEntry>
    {

        private final long ordinal;

        private final long numberOfConsumers;

        public PathTrackingEntryEventHandler( final long ordinal, final long numberOfConsumers )
        {
            this.ordinal = ordinal;
            this.numberOfConsumers = numberOfConsumers;
        }

        public void onEvent( final PathTrackingEntry entry, final long sequence, final boolean endOfBatch )
            throws Exception
        {
            if ( ( sequence % numberOfConsumers ) == ordinal )
            {
                CUBE.doPostBytes( SerializeUtils.serialize( entry ), PathTrackingEntry.class.getName() );
            }
        }

    }

    @Override
    public void store( final PathTrackingEntry pathTrackingEntry )
    {

        ringBuffer.publishEvent( new EventTranslator<PathTrackingEntry>()
        {
            @Override
            public void translateTo( PathTrackingEntry event, long sequence )
            {
                event.setClassName( pathTrackingEntry.getClassName() );
                event.setExecutionTime( pathTrackingEntry.getExecutionTime() );
                event.setLevel( pathTrackingEntry.getLevel() );
                event.setMethodName( pathTrackingEntry.getMethodName() );
                event.setNodeId( pathTrackingEntry.getNodeId() );
                event.setStartTime( pathTrackingEntry.getStartTime() );
                event.setTrackingId( pathTrackingEntry.getTrackingId() );
            }
        } );
    }

    @Override
    protected void pushEntriesByBatch( Map<String, List<Pointer>> pathTrackingEntries )
    {
        if ( !USE_SINGLE_STORE )
        {

            for ( Map.Entry<String, List<Pointer>> entry : pathTrackingEntries.entrySet() )
            {
                for ( Pointer pointer : entry.getValue() )
                {
                    if ( !pointer.isFree() )
                    {
                        CUBE.doPostBytes( readBytes( pointer ), PathTrackingEntry.class.getName() );
                        pointer.freeMemory();
                    }
                }
            }
        }
    }

    public RingBuffer<PathTrackingEntry> getRingBuffer()
    {
        return ringBuffer;
    }

    public void setRingBuffer( RingBuffer<PathTrackingEntry> ringBuffer )
    {
        this.ringBuffer = ringBuffer;
    }

    public int getNumberOfConsumers()
    {
        return numberOfConsumers;
    }

    public void setNumberOfConsumers( int numberOfConsumers )
    {
        this.numberOfConsumers = numberOfConsumers;
    }

    public int getRingBufferSize()
    {
        return ringBufferSize;
    }

    public void setRingBufferSize( int ringBufferSize )
    {
        this.ringBufferSize = ringBufferSize;
    }

    @Destroying
    public void destroy()
    {
        // FIXME timeout??
        disruptor.shutdown();
    }


}
