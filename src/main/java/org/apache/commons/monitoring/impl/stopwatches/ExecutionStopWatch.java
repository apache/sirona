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

package org.apache.commons.monitoring.impl.stopwatches;

import org.apache.commons.monitoring.ExecutionStack;
import org.apache.commons.monitoring.Monitor;

/**
 * Derives from StopWathc default implementation to maintain a list of all
 * topWatches involved in the running process.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ExecutionStopWatch
    extends DefaultStopWatch
{
    /**
     * @param monitor
     */
    public ExecutionStopWatch( Monitor monitor )
    {
        super( monitor );
        ExecutionStack.push( this );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.impl.stopwatches.DefaultStopWatch#stop()
     */
    @Override
    public void stop()
    {
        super.stop();
        if ( ExecutionStack.isTopLevel( this ) )
        {
            ExecutionStack.clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.impl.stopwatches.DefaultStopWatch#cancel()
     */
    @Override
    public void cancel()
    {
        super.cancel();
        if ( ExecutionStack.isTopLevel( this ) )
        {
            ExecutionStack.clear();
        }
    }

}
