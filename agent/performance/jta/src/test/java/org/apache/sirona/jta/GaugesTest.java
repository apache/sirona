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
package org.apache.sirona.jta;

import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.embeddable.EJBContainer;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class GaugesTest {
    private static final int ITERATIONS = 500;

    private static Gauge.LoaderHelper gaugeLoader;

    @BeforeClass
    public static void init() {
        Repository.INSTANCE.clearCounters();
        gaugeLoader = new Gauge.LoaderHelper(false);
    }

    @AfterClass
    public static void reset() {
        Repository.INSTANCE.clearCounters();
        gaugeLoader.destroy();
    }

    @EJB
    private EjbWithJTASupport jtaSupport;

    @Test
    public void test() throws Exception {
        final EJBContainer container = EJBContainer.createEJBContainer(new Properties() {{
            setProperty("openejb.jul.forceReload", Boolean.TRUE.toString());
            setProperty("logging.level.OpenEJB", "OFF"); // logging will make the test failling just cause System.out takes time
        }});
        container.getContext().bind("inject", this);

        final long start = System.currentTimeMillis();

        final CountDownLatch latch = new CountDownLatch(ITERATIONS);
        try {
            final ExecutorService es = Executors.newFixedThreadPool(50);
            for (int i = 0; i < ITERATIONS; i++) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jtaSupport.commit();
                        } catch (final Exception e) {
                            e.printStackTrace(System.out);
                        }

                        try {
                            jtaSupport.rollback();
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            es.shutdown();
            latch.await();

            Thread.sleep(500); // wait last measure

            final long end = System.currentTimeMillis();

            assertEquals(ITERATIONS, sum(Repository.INSTANCE.getGaugeValues(start, end, JTAGauges.JTA_COMMITED).values()), 0);
            assertEquals(ITERATIONS, sum(Repository.INSTANCE.getGaugeValues(start, end, JTAGauges.JTA_ROLLBACKED).values()), 0);

            // due to the sleep we use in commit() we only see half of the tx when checking actives
            assertEquals(ITERATIONS / 2, sum(Repository.INSTANCE.getGaugeValues(start, end, JTAGauges.JTA_ACTIVE).values()), ITERATIONS * .1);
        } finally {
            container.close();
        }
    }

    private static double sum(final Collection<Double> values) {
        double sum = 0;
        for (final Double d : values) {
            sum += d;
        }
        return sum;
    }

    @Singleton
    @Lock(LockType.READ)
    @JTAMonitored
    public static class EjbWithJTASupport {
        public void commit() {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                // no-op
            }
        }

        public void rollback() {
            throw new NullPointerException();
        }
    }
}
