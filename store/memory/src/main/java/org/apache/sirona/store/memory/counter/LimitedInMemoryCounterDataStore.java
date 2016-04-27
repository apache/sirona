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
package org.apache.sirona.store.memory.counter;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.LockableCounter;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

// ensure we don't explode memory cause of web counters, this class will be integrated in sirona > 0.2
public class LimitedInMemoryCounterDataStore extends InMemoryCounterDataStore
{
    private static final int MAX_SIZE = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "counter.max-size", 1000);
    private static final boolean ONLY_EVICT_WEB_COUNTERS = Boolean.parseBoolean(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "counter.evict-web-only", "true"));
    private static final double EVITION_RATIO = Double.parseDouble(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX
            + "counter.evition.ratio", "0.25"));

    @Override
    protected ConcurrentMap<Counter.Key, Counter> newCounterMap() {
        return new FixedSizedMap();
    }

    @Override
    protected Counter newCounter(final Counter.Key key) {
        if (ONLY_EVICT_WEB_COUNTERS) {
            if (Role.WEB.equals(key.getRole())) {
                return new DefaultCounterTimestamped(LockableCounter.class.cast(super.newCounter(key)));
            }
            return super.newCounter(key);
        }
        return new DefaultCounterTimestamped(LockableCounter.class.cast(super.newCounter(key)));
    }

    protected class FixedSizedMap extends ConcurrentSkipListMap<Counter.Key, Counter> {
        protected FixedSizedMap() {
            super(new Comparator<Counter.Key>() {
                @Override
                public int compare(final Counter.Key o1, final Counter.Key o2) {
                    final int role = o1.getRole().compareTo(o2.getRole());
                    if (role == 0) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return role;
                }
            });
        }

        @Override // shouldn't be called
        public Counter put(final Counter.Key key, final Counter value) {
            if (size() >= MAX_SIZE) {
                evict();
            }
            return super.put(key, value);
        }

        @Override
        public Counter putIfAbsent(final Counter.Key key, final Counter value) {
            if (size() >= MAX_SIZE) {
                evict();
            }
            return super.putIfAbsent(key, value);
        }

        private synchronized void evict() {
            if (size() < MAX_SIZE) {
                return;
            }

            final int size = size();
            int toEvict = (int) (size * EVITION_RATIO);
            final List<Entry<Counter.Key, Counter>> entries = new ArrayList<Entry<Counter.Key, Counter>>(size);
            for (final Entry<Counter.Key, Counter> entry : entrySet()) {
                entries.add(entry);
                if (entries.size() >= size) { // size can increase while eviting so ensure we don't force the arraylist to be resized
                    break;
                }
            }

            Collections.sort(entries, new Comparator<Entry<Counter.Key, Counter>>() {
                @Override
                public int compare(final Entry<Counter.Key, Counter> o1, final Entry<Counter.Key, Counter> o2) {
                    final boolean o1HasTimestamp = DefaultCounterTimestamped.class.isInstance(o1);
                    final boolean o2hasTimestamp = DefaultCounterTimestamped.class.isInstance(o2);
                    if (!o1HasTimestamp && !o2hasTimestamp) { // we don't care
                        return o1.getKey().getName().compareTo(o2.getKey().getName());
                    }
                    if (o1HasTimestamp && !o2hasTimestamp) {
                        return -1;
                    }
                    if (!o1HasTimestamp) {
                        return 1;
                    }
                    final long hitDiff = DefaultCounterTimestamped.class.cast(o1.getValue()).timestamp - DefaultCounterTimestamped.class.cast(o2.getValue()).timestamp;
                    return (int) hitDiff;
                }
            });

            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            for (final Entry<Counter.Key, Counter> entry : entries) {
                final Counter.Key key = entry.getKey();
                if (!DefaultCounterTimestamped.class.isInstance(entry.getValue())) {
                    continue;
                }

                final boolean removed = remove(key) != null;
                if (removed) {
					if (gauged) {
                        final Collection<Gauge> g = gauges.remove(key);
						if (g != null) {
							for (final Gauge gauge : g) {
								Repository.INSTANCE.stopGauge(gauge);
							}
						}
					}
                    if (jmx) {
                        try {
                            final ObjectName objectName = LockableCounter.class.cast(entry.getValue()).getJmx();
                            if (server.isRegistered(objectName)) {
                                server.unregisterMBean(objectName);
                            }
                        } catch (final Exception e) {
                            // no-op
                        }
                    }

                    if (toEvict-- <= 0) {
                        break;
                    }
                }
            }
        }
    }

    private static class DefaultCounterTimestamped extends LockableCounter {
        private final LockableCounter delegate;
        private volatile long timestamp = System.currentTimeMillis();

        public DefaultCounterTimestamped(final LockableCounter delegate) {
            super(null, null);
            this.delegate = delegate;
        }

        @Override
        public void addInternal(final double delta) {
            this.delegate.add(delta);
        }

        @Override
        public OptimizedStatistics getStatistics() {
            return this.delegate.getStatistics();
        }

        @Override
        public void setJmx(ObjectName jmx) {
            this.delegate.setJmx(jmx);
        }

        @Override
        public ObjectName getJmx() {
            return this.delegate.getJmx();
        }

        @Override
        public ReadWriteLock getLock() {
            return this.delegate.getLock();
        }

        @Override
        public void reset() {
            this.delegate.reset();
        }

        @Override
        public void add(final double delta) {
            delegate.add(delta);
            timestamp = System.currentTimeMillis();
        }

        @Override
        public void add(final double delta, final Unit unit) {
            this.delegate.add(delta, unit);
        }

        @Override
        public AtomicInteger currentConcurrency() {
            return delegate.currentConcurrency();
        }

        @Override
        public void updateConcurrency(final int concurrency) {
            delegate.updateConcurrency(concurrency);
        }

        @Override
        public int getMaxConcurrency() {
            return delegate.getMaxConcurrency();
        }

        @Override
        public double getMax() {
            return delegate.getMax();
        }

        @Override
        public double getMin() {
            return delegate.getMin();
        }

        @Override
        public long getHits() {
            return delegate.getHits();
        }

        @Override
        public double getSum() {
            return delegate.getSum();
        }

        @Override
        public double getStandardDeviation() {
            return delegate.getStandardDeviation();
        }

        @Override
        public double getVariance() {
            return delegate.getVariance();
        }

        @Override
        public double getMean() {
            return delegate.getMean();
        }

        @Override
        public double getSecondMoment() {
            return delegate.getSecondMoment();
        }

        @Override
        public Key getKey() {
            return delegate.getKey();
        }
    }
}
