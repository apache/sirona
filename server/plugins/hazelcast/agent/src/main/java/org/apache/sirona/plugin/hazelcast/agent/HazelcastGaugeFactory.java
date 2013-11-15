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
package org.apache.sirona.plugin.hazelcast.agent;

import com.hazelcast.core.HazelcastInstance;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeFactory;
import org.apache.sirona.plugin.hazelcast.agent.gauge.HazelcastMembersGauge;
import org.apache.sirona.plugin.hazelcast.agent.gauge.HazelcastPartitionsGauge;
import org.apache.sirona.plugin.hazelcast.agent.instance.HazelcastClientFactory;
import org.apache.sirona.plugin.hazelcast.agent.instance.HazelcastMemberFactory;

import java.util.Collection;
import java.util.LinkedList;

public class HazelcastGaugeFactory implements GaugeFactory {
    @Override
    public Gauge[] gauges() {
        final String instances = Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "hazelcast.clusters", "");
        if (instances.isEmpty()) {
            return null;
        }

        final Collection<Gauge> gauges = new LinkedList<Gauge>();
        for (final String instance : instances.split(",")) {
            final String trimmed = instance.trim();

            final HazelcastInstance hzInstance;
            final String prefix = Configuration.CONFIG_PROPERTY_PREFIX + "hazelcast." + trimmed + ".";
            if (Configuration.is(prefix + "client", true)) {
                hzInstance = HazelcastClientFactory.newClient(prefix);
            } else {
                hzInstance = HazelcastMemberFactory.newMember(trimmed, prefix);
            }

            IoCs.processInstance(new DestroyInstance(hzInstance)); // ensure to shutdown

            addGauges(gauges, trimmed, hzInstance);
        }
        return gauges.toArray(new Gauge[gauges.size()]);
    }

    private void addGauges(final Collection<Gauge> gauges, final String name, final HazelcastInstance hzInstance) {
        gauges.add(new HazelcastMembersGauge(name, hzInstance));
        gauges.add(new HazelcastPartitionsGauge(name, hzInstance));
    }

    private static class DestroyInstance {
        private final HazelcastInstance instance;

        public DestroyInstance(final HazelcastInstance instance) {
            this.instance = instance;
        }

        @Destroying
        public void destroy() {
            instance.getLifecycleService().shutdown();
        }
    }
}
