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
package org.apache.sirona.cassandra.framework;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.List;

public class CassandraRunner extends BlockJUnit4ClassRunner {
    private static final String CLUSTER = "TestCluster";

    public CassandraRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule(){
            public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
                return new CassandraLifecycle(target, base);
            }
        });
        return rules;
    }

    public class CassandraLifecycle extends Statement {
        private final Statement next;
        private final Object instance;

        public CassandraLifecycle(final Object target, final Statement next) {
            this.next = next;
            this.instance = target;
        }

        @Override
        public void evaluate() throws Throwable { // one clean instance by test to avoid side effects
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            try {
                final Cluster cluster = HFactory.getOrCreateCluster("TestCluster", DatabaseDescriptor.getRpcAddress().getHostName() + ":" + DatabaseDescriptor.getRpcPort());

                for (final Field f : instance.getClass().getDeclaredFields()) {
                    final CassandraTestInject annotation = f.getAnnotation(CassandraTestInject.class);
                    if (annotation != null) {
                        if (Cluster.class.equals(f.getType())) {
                            f.setAccessible(true);
                            f.set(instance, cluster);
                        } else if (Keyspace.class.equals(f.getType())) {
                            f.setAccessible(true);
                            f.set(instance, HFactory.createKeyspace(CLUSTER, cluster));
                        }
                    }
                }
                next.evaluate();
            } finally {
                EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
                Repository.INSTANCE.reset();
                IoCs.shutdown();
            }
        }
    }
}
