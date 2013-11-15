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
import org.apache.sirona.jpa.entity.Person;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class QueryTest {
    @Before
    @After
    public void reset() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void simpleFind() {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jse");
        assertNotNull(emf);
        reset(); // get rid of init counter

        try {
            final EntityManager em = emf.createEntityManager();
            assertNotNull(em);

            assertCounter("createEntityManager");

            reset(); // get rid of createEntityManager counter
            try {
                em.find(Person.class, 0L);
                assertCounter("find");
                reset(); // get rid of em.find() counter
            } finally {
                em.close();
                assertCounter("EntityManagerImpl.close");
            }

            reset(); // get rid of em.close() counter
        } finally {
            emf.close();
        }
        assertCounter("EntityManagerFactoryImpl.close");
    }

    @Test
    public void createNamedQuery() {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jse");

        try {
            { // init
                final EntityManager em = emf.createEntityManager();
                try {
                    final EntityTransaction transaction = em.getTransaction();
                    transaction.begin();
                    try {
                        final Person p = new Person();
                        p.setName("sirona");

                        em.persist(p);
                        transaction.commit();
                    } catch (final Exception e) {
                        transaction.rollback();
                    }
                } finally {
                    em.close();
                }
            }

            { // checks
                final EntityManager em = emf.createEntityManager();
                try {
                    reset();
                    final TypedQuery<Person> namedQuery = em.createNamedQuery("Person.findByName", Person.class);
                    assertCounter("createNamedQuery");
                    namedQuery.setParameter("name", "sirona").getSingleResult();
                } finally {
                    em.close();
                }
            }
        } finally {
            emf.close();
        }
    }

    private static void assertCounter(final String method) {
        assertTrue(Repository.INSTANCE.counters().iterator().hasNext());

        final Counter counter = Repository.INSTANCE.counters().iterator().next();
        assertEquals(1, counter.getHits());
        assertThat(counter.getKey().getName(), containsString(method));
    }
}
