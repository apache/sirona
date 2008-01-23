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

package org.apache.commons.monitoring.impl;

public class ConcurrencyGauge
    extends SimpleValue
{
    private long cpuTime;

    private long lastUse;

    private long firstUse;

    @Override
    public synchronized void increment()
    {
        long now = nanoTime();
        if ( firstUse == 0 )
        {
            firstUse = now;
        }
        if ( lastUse > 0 )
        {
            long delta = now - lastUse;
            cpuTime += get() * delta;
        }
        super.increment();
        lastUse = now;
    }

    protected long nanoTime()
    {
        return System.nanoTime();
    }

    @Override
    public synchronized void decrement()
    {
        long now = nanoTime();
        long delta = now - lastUse;
        cpuTime += get() * delta;
        super.decrement();
        lastUse = now;
    }

    @Override
    public synchronized double average()
    {
        return ( (double) cpuTime ) / ( lastUse - firstUse );
    }
}
