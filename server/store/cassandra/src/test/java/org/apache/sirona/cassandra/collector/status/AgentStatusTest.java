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
package org.apache.sirona.cassandra.collector.status;

import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(CassandraRunner.class)
public class AgentStatusTest {
    @Test
    public void checkPersistence() {
        new CassandraCollectorNodeStatusDataStore().store("node1", new NodeStatus(new ValidationResult[] {
            new ValidationResult("v1", Status.OK, "m1"), new ValidationResult("v2", Status.KO, "m2") }, new Date()));
        new CassandraCollectorNodeStatusDataStore().store("node2", new NodeStatus(new ValidationResult[] {
            new ValidationResult("v3", Status.OK, "m3"), new ValidationResult("v4", Status.KO, "m4") }, new Date()));

        final Map<String, NodeStatus> statuses = new CassandraCollectorNodeStatusDataStore().statuses();

        assertEquals(2, statuses.size());

        final NodeStatus n1 = statuses.get("node1");
        assertNotNull(n1);

        final ValidationResult[] result1 = n1.getResults();
        assertEquals(2, result1.length);
        assertEquals("v1", result1[0].getName());
        assertEquals("m1", result1[0].getMessage());
        assertEquals(Status.OK, result1[0].getStatus());
        assertEquals("v2", result1[1].getName());
        assertEquals("m2", result1[1].getMessage());
        assertEquals(Status.KO, result1[1].getStatus());

        final NodeStatus n2 = statuses.get("node2");
        assertNotNull(n2);

        final ValidationResult[] result2 = n2.getResults();
        assertEquals(2, result2.length);
        assertEquals("v3", result2[0].getName());
        assertEquals("m3", result2[0].getMessage());
        assertEquals(Status.OK, result2[0].getStatus());
        assertEquals("v4", result2[1].getName());
        assertEquals("m4", result2[1].getMessage());
        assertEquals(Status.KO, result2[1].getStatus());
    }
}
