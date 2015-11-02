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
package org.apache.sirona.websocket;

import org.apache.catalina.startup.Constants;
import org.apache.johnzon.websocket.mapper.JohnzonTextDecoder;
import org.apache.sirona.Role;
import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.DefaultRepository;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.NodeStatusReporter;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.websocket.client.WebSocketCounterDataStore;
import org.apache.sirona.websocket.client.WebSocketGaugeDataStore;
import org.apache.sirona.websocket.client.WebSocketNodeStatusDataStore;
import org.apache.sirona.websocket.server.CounterEndpoint;
import org.apache.sirona.websocket.server.GaugeEndpoint;
import org.apache.sirona.websocket.server.ValidationEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import static java.lang.Thread.sleep;
import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WebSocketTest {
    static {
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*,r*,sa*,se*,sh*,su*,v*,w*,x*,z*");
        System.setProperty(Constants.TLD_JARS_TO_SKIP, "*");
    }

    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "test-websocket.war")
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "sirona-websocket.jar")
                    .addClasses(CounterEndpoint.class, GaugeEndpoint.class, ValidationEndpoint.class))
            .addAsLibraries(jarLocation(JohnzonTextDecoder.class));
    }

    @ArquillianResource
    protected URL base;

    @Before
    @After
    public void reset() {
        IoCs.findOrCreateInstance(Repository.class).reset();
    }

    @Test
    public void runFewClientActions() {
        // create another repo to keep the main one for the server but use the client stores
        // to ensure we send to the server with out client
        final Date date = new Date();
        final Repository clientRepo = new DefaultRepository(
            IoCs.processInstance(new WebSocketCounterDataStore() {
                @Override
                protected int getPeriod(final String name) {
                    return 1000;
                }
            }),
            IoCs.processInstance(new WebSocketGaugeDataStore() {
                @Override
                protected int getPeriod(final String name) {
                    return 1000;
                }
            }),
            IoCs.processInstance(new WebSocketNodeStatusDataStore() {
                @Override
                protected int getPeriod(final String name) {
                    return 1000;
                }

                @Override
                protected NodeStatusReporter newNodeStatusReporter() {
                    return new NodeStatusReporter() {
                        @Override
                        public synchronized NodeStatus computeStatus() {
                            return new NodeStatus(new ValidationResult[] {
                                new ValidationResult("v1", Status.OK, "all is fine"),
                                new ValidationResult("v2", Status.KO, "oops something went wrong")
                            }, date);
                        }
                    };
                }
            }),
            new InMemoryPathTrackingDataStore(),
            Collections.<AlertListener>emptyList()
        ) {};
        clientRepo.addGauge(new Gauge() {
            @Override
            public Role role() {
                return new Role("testgauge", Unit.HECTO);
            }

            @Override
            public double value() {
                return 10.;
            }
        });
        clientRepo.getCounter(new Counter.Key(new Role("c", Unit.Time.MINUTE), "testcounter")).add(20);

        try { // period = 1s in sirona.properties so wait 2 periods to ensure to have data
            sleep(2500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        { // check gauge
            final Collection<Role> gauges = Repository.INSTANCE.gauges();
            assertEquals(1, gauges.size());

            final Iterator<Role> iterator = gauges.iterator();
            final Role next = iterator.next();
            assertEquals("testgauge", next.getName());
            assertEquals("*100", next.getUnit().getName());

            final long now = System.currentTimeMillis();
            final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues(now - 3000, now, next);
            assertTrue(gauges.size() > 0);
            assertEquals(10., gaugeValues.get(gaugeValues.firstKey()), 0.);
        }
        { // check counter
            final Collection<Counter> counters = Repository.INSTANCE.counters();
            assertEquals(1, counters.size());
            final Counter counter = counters.iterator().next();
            assertEquals("testcounter", counter.getKey().getName());
            assertEquals("c", counter.getKey().getRole().getName());
            assertEquals("min", counter.getKey().getRole().getUnit().getName());
            assertEquals(20., counter.getSum(), 0.);
            assertEquals(1, counter.getHits());
        }
        { // check validation
            final Map<String, NodeStatus> statuses = Repository.INSTANCE.statuses();
            assertEquals(1, statuses.size());
            assertTrue(statuses.containsKey("test"));

            final NodeStatus status = statuses.get("test");
            assertTrue(Math.abs(date.getTime() - status.getDate().getTime()) < 1250);
            assertEquals(Status.KO, status.getStatus());
            assertEquals(2, status.getResults().length);
            assertEquals("v1", status.getResults()[0].getName());
            assertEquals(Status.OK, status.getResults()[0].getStatus());
            assertEquals("all is fine", status.getResults()[0].getMessage());
            assertEquals("v2", status.getResults()[1].getName());
            assertEquals(Status.KO, status.getResults()[1].getStatus());
            assertEquals("oops something went wrong", status.getResults()[1].getMessage());
        }
    }
}
