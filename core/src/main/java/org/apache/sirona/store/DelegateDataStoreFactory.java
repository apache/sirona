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

import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;

public class DelegateDataStoreFactory implements DataStoreFactory {
    private final CounterDataStore counterDataStore;
    private final CommonGaugeDataStore gaugeDataStore;
    private final NodeStatusDataStore nodeStatusDataStore;
    private final PathTrackingDataStore pathTrackingDataStore;

    public DelegateDataStoreFactory(final CounterDataStore counterDataStore,
                                    final CommonGaugeDataStore gaugeDataStore,
                                    final NodeStatusDataStore nodeStatusDataStore,
                                    final PathTrackingDataStore pathTrackingDataStore) {
        this.counterDataStore = counterDataStore;
        this.gaugeDataStore = gaugeDataStore;
        this.nodeStatusDataStore = nodeStatusDataStore;
        this.pathTrackingDataStore = pathTrackingDataStore;
    }

    @Override
    public CounterDataStore getCounterDataStore() {
        return counterDataStore;
    }

    @Override
    public CommonGaugeDataStore getGaugeDataStore() {
        return gaugeDataStore;
    }

    @Override
    public NodeStatusDataStore getNodeStatusDataStore() {
        return nodeStatusDataStore;
    }

    @Override
    public PathTrackingDataStore getPathTrackingDataStore() {
        return pathTrackingDataStore;
    }
}
