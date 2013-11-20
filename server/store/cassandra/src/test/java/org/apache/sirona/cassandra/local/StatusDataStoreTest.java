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
package org.apache.sirona.cassandra.local;

import org.apache.sirona.cassandra.agent.status.CassandraStatusDataStore;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.NodeStatusReporter;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(CassandraRunner.class)
public class StatusDataStoreTest {
    @Test
    public void statuses() throws InterruptedException, IllegalAccessException {
        final CassandraStatusDataStore store = IoCs.processInstance(new CassandraStatusDataStore() {
            @Created
            protected void forceMarker() {
                marker = "test";
            }

            @Override
            protected NodeStatusReporter newNodeStatusReporter() {
                return new NodeStatusReporter() {
                    @Override
                    public synchronized NodeStatus computeStatus() {
                        return new NodeStatus(new ValidationResult[]{new ValidationResult("sample", Status.OK, "msg")}, new Date());
                    }
                };
            }

            @Override
            protected int getPeriod(final String prefix) {
                return 100;
            }
        });

        assertEquals(0, store.statuses().size());

        Thread.sleep(250);
        store.shutdown();

        final Map<String,NodeStatus> statuses = store.statuses();
        assertEquals(1, statuses.size());
        assertTrue(statuses.containsKey("test"));

        final ValidationResult[] results = statuses.get("test").getResults();
        assertEquals(1, results.length);

        final ValidationResult validationResult = results[0];
        assertEquals("sample", validationResult.getName());
        assertEquals("msg", validationResult.getMessage());
        assertEquals(Status.OK, validationResult.getStatus());
    }
}
