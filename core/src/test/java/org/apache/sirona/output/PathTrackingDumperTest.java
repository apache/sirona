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
package org.apache.sirona.output;

import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class PathTrackingDumperTest {
    @Test
    public void run() {
        final PathTrackingDataStore store = new InMemoryPathTrackingDataStore();
        {
            final PathTrackingEntry entry = new PathTrackingEntry();
            entry.setTrackingId("t1");
            entry.setClassName("com.myclass.Foo");
            entry.setMethodName("bar");
            entry.setLevel(1);
            entry.setNodeId("n1");
            entry.setStartTime(TimeUnit.DAYS.toNanos(1));
            entry.setExecutionTime(TimeUnit.MILLISECONDS.toNanos(121));
            store.store(entry);
        }
        {
            final PathTrackingEntry entry = new PathTrackingEntry();
            entry.setTrackingId("t1");
            entry.setClassName("com.myclass.Dummy");
            entry.setMethodName("drums");
            entry.setLevel(2);
            entry.setNodeId("n2");
            entry.setStartTime(TimeUnit.DAYS.toNanos(1) + TimeUnit.MILLISECONDS.toNanos(12));
            entry.setExecutionTime(TimeUnit.MILLISECONDS.toNanos(43));
            store.store(entry);
        }
        {
            final PathTrackingEntry entry = new PathTrackingEntry();
            entry.setTrackingId("t2");
            entry.setClassName("com.myclass.Alone");
            entry.setMethodName("single");
            entry.setLevel(1);
            entry.setNodeId("n9");
            entry.setStartTime(TimeUnit.DAYS.toNanos(2));
            entry.setExecutionTime(TimeUnit.MILLISECONDS.toNanos(89));
            store.store(entry);
        }

        final PathTrackingDumper dumper = new PathTrackingDumper(store, true);
        assertEquals(
            "1 com.myclass.Foo#bar -> 121ms\n" +
            "  2 com.myclass.Dummy#drums -> 43ms\n" +
            "1 com.myclass.Alone#single -> 89ms\n",
            dumper.dumpAsString(new Date(0), new Date()));
    }
}
