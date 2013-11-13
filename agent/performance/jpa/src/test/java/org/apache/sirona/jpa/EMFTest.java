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
package org.apache.sirona.jpa;

import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitInfo;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EMFTest {
    @Before
    @After
    public void reset() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void newEmfJSe() {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jse");
        assertNotNull(emf);
        emf.close();

        assertCreateCalled();
    }

    @Test
    public void newEmfJavaEE() {
        final EntityManagerFactory emf = new SironaPersistence().createContainerEntityManagerFactory(
            PersistenceUnitInfo.class.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PersistenceUnitInfo.class}, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    if (boolean.class.equals(method.getReturnType())) {
                        return false;
                    }
                    return null;
                }
            })), null);
        assertNotNull(emf);
        emf.close();

        assertCreateCalled();
    }

    private static void assertCreateCalled() {
        final Collection<Counter> counters = Repository.INSTANCE.counters();
        assertEquals(1, counters.size());

        final Counter counter = counters.iterator().next();
        assertEquals(1, counter.getHits());
    }
}
