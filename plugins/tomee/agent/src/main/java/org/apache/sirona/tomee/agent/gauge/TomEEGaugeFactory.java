/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.tomee.agent.gauge;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Pool;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeFactory;
import org.apache.sirona.tomee.agent.Reflection;

import java.util.Collection;
import java.util.LinkedList;

public class TomEEGaugeFactory implements GaugeFactory, Reflection {
    @Override
    public Gauge[] gauges() {
        final Collection<Gauge> gauges = new LinkedList<Gauge>();

        if (Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "tomee.gauges.activated", true)) {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            for (final BeanContext beanContext : containerSystem.deployments()) {
                if (!beanContext.isHidden() && BeanType.STATELESS.equals(beanContext.getComponentType()) && beanContext.getContainerData() != null) {
                    final Object data = beanContext.getContainerData();
                    final Pool<?> pool = Pool.class.cast(Reflections.invokeByReflection(data, "getPool", NO_PARAM_TYPES, NO_PARAM));
                    final Object stats = Reflections.get(pool, "stats");
                    final String name = String.class.cast(beanContext.getDeploymentID());
                    gauges.add(new PoolGauge(name, stats, "getInstancesPooled"));
                    gauges.add(new PoolGauge(name, stats, "getInstancesActive"));
                }
            }
        }

        return gauges.toArray(new Gauge[gauges.size()]);
    }
}
