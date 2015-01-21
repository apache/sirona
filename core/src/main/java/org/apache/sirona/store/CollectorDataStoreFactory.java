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
package org.apache.sirona.store;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.DelegatedCollectorGaugeDataStore;
import org.apache.sirona.store.status.InMemoryCollectorNodeStatusDataStore;
import org.apache.sirona.store.tracking.DelegatedCollectorPathTrackingDataStore;

public class CollectorDataStoreFactory
    extends DelegateDataStoreFactory
{
    public CollectorDataStoreFactory()
    {
        super( IoCs.findOrCreateInstance( CounterDataStore.class ), //
               new DelegatedCollectorGaugeDataStore(), //
               new InMemoryCollectorNodeStatusDataStore(), //
               new DelegatedCollectorPathTrackingDataStore() );
    }
}
