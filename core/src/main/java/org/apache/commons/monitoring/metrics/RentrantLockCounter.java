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

package org.apache.commons.monitoring.metrics;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Role;

/**
 * Thread-safe implementation of <code>Counter</code>.
 * <p>
 * To reduce impact of synchronization, only the internal counter update is synchronized. Events dispatching is not and
 * listeners must handle concurrency.
 * <p>
 * Implementation note : According to http://forums.sun.com/thread.jspa?threadID=5341435, use of
 * ReentrantLock has fiewer impact on performances than language-level synchronization.
 * @see CounterBench
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class RentrantLockCounter
    extends ThreadSafeCounter
    implements Counter
{

    ReentrantLock lock = new ReentrantLock();

    public RentrantLockCounter( Role role )
    {
        super( role );
    }

    protected void threadSafeAdd( double delta )
    {
        lock.lock();
        try
        {
            doThreadSafeAdd( delta );
        }
        finally
        {
            lock.unlock();
        }
    }

    public void reset()
    {
        lock.lock();
        try
        {
            doReset();
        }
        finally
        {
            lock.unlock();
        }
    }


}
