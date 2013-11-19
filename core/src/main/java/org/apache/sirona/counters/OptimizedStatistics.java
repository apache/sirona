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
package org.apache.sirona.counters;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class OptimizedStatistics extends SummaryStatistics {
    public OptimizedStatistics() {
        // we don't use sumsq and sumlog so mock them to gain a lot of time in concurrent environments
        setSumsqImpl(NoopStat.INSTANCE);
        setSumLogImpl(NoopStat.INSTANCE);
    }
}
