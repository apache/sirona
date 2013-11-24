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
package org.apache.test.sirona.javaagent;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.repositories.Repository;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JavaAgentRunner.class)
public class SimpleTest {
    @Test
    public void counters() {
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.noReturn", 0);

        new ServiceTransform().noReturn();
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.noReturn", 1);

        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.withReturn", 0);
        new ServiceTransform().withReturn();
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.withReturn", 1);

        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.nest", 0);
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$Service2Transform.nested", 0);
        new ServiceTransform().nest();
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$ServiceTransform.nest", 1);
        assertHits("org.apache.test.sirona.javaagent.SimpleTest$Service2Transform.nested", 1);
    }

    private void assertHits(final String name, final int expected) {
        assertEquals(expected, Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, name)).getHits());
    }

    public static class ServiceTransform {
        public void noReturn() {

        }

        public String withReturn() {
            return "ok";
        }

        public String nest() {
            return new Service2Transform().nested();
        }
    }

    public static class Service2Transform {
        public String nested() {
            return null;
        }
    }
}
