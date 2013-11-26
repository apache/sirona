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
import org.apache.sirona.javaagent.listener.CounterListener;
import org.apache.sirona.repositories.Repository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CounterListenerConfigTest {
    private static final Counter.Key KEY = new Counter.Key(Role.PERFORMANCES, "org.apache.test.sirona.javaagent.CounterListenerConfigTest$Foo.fake");

    private CounterListener listener;

    @Before
    public void before() {
        listener = new CounterListener();
        Repository.INSTANCE.clearCounters();
    }

    @AfterClass
    public static void clear() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void include() {
        listener.setExcludes(null);
        listener.setIncludes("prefix:" + KEY.getName());
        assertTrue(listener.accept(KEY, null));
    }

    @Test
    public void exclude() {
        listener.setExcludes("prefix:" + KEY.getName());
        listener.setIncludes(null);
        assertFalse(listener.accept(KEY, null));
    }

    @Test
    public void includeExclude() {
        listener.setExcludes("prefix:" + KEY.getName());
        listener.setIncludes("prefix:" + KEY.getName());
        assertFalse(listener.accept(KEY, null));

        listener.setExcludes("prefix:" + KEY.getName() + "_");
        listener.setIncludes("prefix:" + KEY.getName());
        assertTrue(listener.accept(KEY, null));
    }
}
