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
package org.apache.sirona.repositories;

import org.apache.sirona.SironaException;
import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.NodeStatusReporter;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.status.PeriodicNodeStatusDataStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class DefaultRepositoryTest {
    @Test
    public void alerts() {
        if (Alerter1.instance != null && Alerter1.instance.alerts != null) {
            Alerter1.instance.alerts.clear();
        }
        if (Alerter2.instance != null && Alerter2.instance.alerts != null) {
            Alerter2.instance.alerts.clear();
        }

        NodeStatusDataStore original;
        try {
            original = IoCs.findOrCreateInstance(NodeStatusDataStore.class);
        } catch (final SironaException se) {
            original = null;
        }

        final ForTestPeriodicNodeStatusDataStore testNodeStatusStore = new ForTestPeriodicNodeStatusDataStore();
        IoCs.setSingletonInstance(NodeStatusDataStore.class, testNodeStatusStore);
        try {
            new DefaultRepository(); // create alerters
            assertEquals("a-config-value", Alerter1.instance.config);
            assertNotNull(Alerter1.instance);
            assertEquals(0, Alerter1.instance.alerts.size());
            assertNotNull(Alerter2.instance);
            assertEquals(0, Alerter2.instance.alerts.size());

            testNodeStatusStore.periodicTask();
            assertEquals(1, Alerter1.instance.alerts.size());
            assertEquals(1, Alerter2.instance.alerts.size());
            assertSame(Alerter1.instance.alerts.iterator().next(), Alerter2.instance.alerts.iterator().next());
        } finally {
            IoCs.setSingletonInstance(NodeStatusDataStore.class, original);
        }
    }

    public static class Alerter1 implements AlertListener {
        private static volatile Alerter1 instance;

        private final Collection<Alert> alerts = new ArrayList<Alert>();

        private String config;

        public Alerter1() {
            instance = this;
        }

        public void onAlert(final Alert alert) {
            alerts.add(alert);
        }
    }
    public static class Alerter2 implements AlertListener {
        private static volatile Alerter2 instance;
        private final Collection<Alert> alerts = new ArrayList<Alert>();

        public Alerter2() {
            instance = this;
        }

        public void onAlert(final Alert alert) {
            alerts.add(alert);
        }
    }

    public static class ForTestPeriodicNodeStatusDataStore extends PeriodicNodeStatusDataStore {
        @Override
        protected int getPeriod(final String name) {
            return -1;
        }

        @Override
        public void periodicTask() {
            super.periodicTask();
        }

        @Override
        protected NodeStatusReporter newNodeStatusReporter() {
            return new NodeStatusReporter() {
                @Override
                public synchronized NodeStatus computeStatus() {
                    return new NodeStatus(new ValidationResult[] { new ValidationResult("ko", Status.KO, "")}, new Date());
                }
            };
        }
    }
}
