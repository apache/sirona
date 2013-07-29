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
package org.apache.commons.monitoring.counter.queuemanager;

import org.apache.commons.monitoring.counter.DefaultCounter;

import java.util.concurrent.locks.Lock;

/**
 * An alternative using Disruptor would need (take care it doesn't go in the shade):
 *
 *

 <dependency>
     <groupId>com.lmax</groupId>
     <artifactId>disruptor</artifactId>
     <version>3.1.1</version>
     <optional>true</optional>
 </dependency>

 * look like:
 *
 *

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.monitoring.counter.DefaultCounter;
import org.apache.commons.monitoring.util.DaemonThreadFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DisruptorMetricQueueManager implements MetricQueueManager, Closeable {
    private static final Logger LOGGER = Logger.getLogger(DefaultCounter.class.getName());

    private static final int RINGBUFFER_DEFAULT_SIZE = 256 * 1024;
    private static final long HALF_A_SECOND = 500;
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 20;

    private static final Disruptor<MetricEvent> DISRUPTOR;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new DaemonThreadFactory("commons-monitoring-")); // thread safety using a single thread ;)
    static {
        DISRUPTOR = new Disruptor<MetricEvent>(Factory.INSTANCE, RINGBUFFER_DEFAULT_SIZE, EXECUTOR, ProducerType.MULTI, new SleepingWaitStrategy());
        DISRUPTOR.handleExceptionsWith(new MetricEventExceptionHandler());
        DISRUPTOR.handleEventsWith(new MetricEventHandler());
        DISRUPTOR.start();
    }

    public static boolean flushRingBuffer() {
        final RingBuffer<MetricEvent> ringBuffer = DISRUPTOR.getRingBuffer();
        for (int i = 0; i < MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN; i++) {
            if (ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize())) {
                break;
            }

            try {
                Thread.sleep(HALF_A_SECOND);
            } catch (final InterruptedException e) {
                // no-op
            }
        }
        return ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }

    @Override
    public void add(final DefaultCounter metric, final double delta) {
        DISRUPTOR.publishEvent(new MetricEventTranslator(metric, delta));
    }

    @Override
    public void close() throws IOException {
        DISRUPTOR.shutdown();
        flushRingBuffer();

        EXECUTOR.shutdown();
        try {
            EXECUTOR.awaitTermination(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            // no-op
        }
    }

    private static class MetricEvent {
        private DefaultCounter metric;
        private double value;
    }

    private static class Factory implements EventFactory<MetricEvent> {
        public static final Factory INSTANCE = new Factory();

        @Override
        public MetricEvent newInstance() {
            return new MetricEvent();
        }
    }

    private class MetricEventTranslator implements EventTranslator<MetricEvent> {
        private final double value;
        private final DefaultCounter metric;

        public MetricEventTranslator(final DefaultCounter defaultCounter, final double delta) {
            this.metric = defaultCounter;
            this.value = delta;
        }

        @Override
        public void translateTo(final MetricEvent event, final long sequence) {
            event.metric = metric;
            event.value = value;
        }
    }

    private static class MetricEventHandler implements EventHandler<MetricEvent> {
        @Override
        public void onEvent(final MetricEvent event, final long sequence, final boolean endOfBatch) throws Exception {
            event.metric.addInternal(event.value);
        }
    }

    private static class MetricEventExceptionHandler implements ExceptionHandler {
        @Override
        public void handleEventException(final Throwable ex, final long sequence, final Object event) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        @Override
        public void handleOnStartException(final Throwable ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        @Override
        public void handleOnShutdownException(final Throwable ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}

*/
public class DefaultMetricQueueManager implements MetricQueueManager {
    @Override
    public void add(final DefaultCounter baseMetrics, final double delta) {
        final Lock lock = baseMetrics.getLock();
        lock.lock();
        try {
            baseMetrics.addInternal(delta);
        } finally {
            lock.unlock();
        }
    }
}
