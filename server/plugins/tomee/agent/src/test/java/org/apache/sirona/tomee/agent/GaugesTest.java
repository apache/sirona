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
package org.apache.sirona.tomee.agent;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.status.PeriodicNodeStatusDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class GaugesTest {
    private Gauge.LoaderHelper loader = null;

    @EJB
    private AStatelessBean bean;

    @Module
    public EnterpriseBean bean() {
        return new StatelessBean(AStatelessBean.class).localBean();
    }

    @Before
    public void registerGauges() {
        Repository.INSTANCE.reset();
        loader = new Gauge.LoaderHelper(false);
    }

    @After
    public void unregisterGauges() {
        if (loader != null) {
            loader.destroy();
        }
        Repository.INSTANCE.reset();
        PeriodicNodeStatusDataStore.class.cast(IoCs.getInstance(NodeStatusDataStore.class)).shutdown();
    }

    @Test
    public void checkGauges() throws InterruptedException {
        final Role gaugeRole = Repository.INSTANCE.findGaugeRole("tomee-pool-stateless-instancesPooled-AStatelessBean");
        final Role gaugeActiveRole = Repository.INSTANCE.findGaugeRole("tomee-pool-stateless-instancesActive-AStatelessBean");
        assertNotNull(gaugeRole);
        assertNotNull(gaugeActiveRole);
        assertEquals(0, sum(gaugeRole));
        assertEquals(0, sum(gaugeActiveRole));
        bean.work();
        Thread.sleep(125);
        assertEquals(1, sum(gaugeRole));
        // hard to get active > 0 in this test
    }

    @Stateless
    public static class AStatelessBean {
        public void work() {}
    }

    private static int sum(final Role gaugeRole) {
        int i = 0;
        for (final Double v : Repository.INSTANCE.getGaugeValues(0, System.currentTimeMillis(), gaugeRole).values()) {
            i += v;
        }
        return i;
    }
}
