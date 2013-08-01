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
package org.apache.commons.monitoring.cdi;

import org.apache.commons.monitoring.counter.Role;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.webbeans.cditest.CdiTestContainer;
import org.apache.webbeans.cditest.CdiTestContainerLoader;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommonsMonitoringInterceptorTest {
    @Test
    public void checkMeasures() throws Exception {
        final CdiTestContainer container = CdiTestContainerLoader.getCdiContainer();
        container.bootContainer();
        container.startApplicationScope();

        final BeanManager beanManager = container.getBeanManager();
        final MonitoredBean bean = MonitoredBean.class.cast(beanManager.getReference(beanManager.resolve(beanManager.getBeans(MonitoredBean.class)), MonitoredBean.class, null));

        bean.twoSeconds();

        container.stopApplicationScope();
        container.shutdownContainer();

        final Counter perf = Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, MonitoredBean.class.getName() + ".twoSeconds"));
        assertNotNull(perf);
        assertEquals(2000, TimeUnit.NANOSECONDS.toMillis((int) perf.getMax()), 200);
    }

    @Monitored
    @ApplicationScoped
    public static class MonitoredBean {
        public void twoSeconds() {
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // no-op
            }
        }
    }
}
