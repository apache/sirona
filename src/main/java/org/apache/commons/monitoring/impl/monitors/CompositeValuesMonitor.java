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

package org.apache.commons.monitoring.impl.monitors;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.values.CompositeCounter;
import org.apache.commons.monitoring.impl.values.CompositeGauge;

/**
 * A Monitor implementation that creates {@link Composite} Gauges and Counters.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompositeValuesMonitor extends CreateValuesOnDemandMonitor
{

    public CompositeValuesMonitor( Key key )
    {
        super( key );
    }

    @Override
    protected Counter newCounterInstance( String role )
    {
        return new CompositeCounter( role );
    }

    @Override
    protected Gauge newGaugeInstance( String role )
    {
        return new CompositeGauge( role );
    }

}
