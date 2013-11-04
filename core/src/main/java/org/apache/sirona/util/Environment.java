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
package org.apache.sirona.util;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.CollectorCounterStore;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.CounterDataStore;

public final class Environment {
    private Environment() {
        // no-op
    }

    public static boolean isCollector() {
        Configuration.findOrCreateInstance(Repository.class); // ensure we have a repo which init store classes by default
        return Configuration.getInstance(CollectorCounterStore.class) != null;
    }
}
