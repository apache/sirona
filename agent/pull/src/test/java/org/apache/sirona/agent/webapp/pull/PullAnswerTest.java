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
package org.apache.sirona.agent.webapp.pull;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.sirona.Role;
import org.apache.sirona.agent.webapp.pull.repository.PullRepository;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PullAnswerTest {
    @Test
    public void json() {
        final JSONArray snapshotJson = JSONArray.class.cast(
            JSONSerializer.toJSON(createRepo().snapshot().replaceAll("\"time\": \"[^\"]*\"", "\"time\": \"assert\"").replaceAll("\"marker\": \"[^\"]*\"", "\"marker\": \"ubuntu\"")));
        assertEquals(11, snapshotJson.size());

        final Collection<Integer> counters = new LinkedList<Integer>();
        final Collection<String> gauges = new LinkedList<String>();
        final Collection<String> validations = new LinkedList<String>();
        final Collection<Long> statuses = new LinkedList<Long>();
        for (int i = 0; i < 11; i++) {
            final JSONObject object = JSONObject.class.cast(snapshotJson.get(i));
            final Object type = object.get("type");
            final JSONObject data = JSONObject.class.cast(object.get("data"));
            if ("counter".equals(type)) {
                counters.add(Number.class.cast(data.get("max")).intValue());
            } if ("gauge".equals(type)) {
                gauges.add(String.class.cast(data.get("role")));
            } if ("validation".equals(type)) {
                validations.add(String.class.cast(data.get("name")));
            } if ("status".equals(type)) {
                statuses.add(Number.class.cast(data.get("date")).longValue());
            }
        }

        assertEquals(1, statuses.size());

        assertEquals(3, counters.size());
        assertTrue(counters.contains(0));
        assertTrue(counters.contains(1));
        assertTrue(counters.contains(2));

        assertTrue(gauges.contains("gaugerole"));

        assertTrue(validations.contains("fake"));
        assertTrue(validations.contains("refake"));
    }

    private static PullRepository createRepo() {
        final PullRepository repo = new PullRepository();
        for (int i = 0; i < 3; i++) {
            repo.getCounter(new Counter.Key(Role.PERFORMANCES, "counter#" + i)).add(i);
        }
        repo.addGauge(new Gauge() {
            public int value = 0;

            @Override
            public Role role() {
                return new Role("gaugerole", Unit.UNARY);
            }

            @Override
            public double value() {
                return value++;
            }
        });
        return repo;
    }
}
