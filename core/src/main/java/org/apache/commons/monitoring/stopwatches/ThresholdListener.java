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

package org.apache.commons.monitoring.stopwatches;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ThresholdListener
    implements HistoryOfMyThread.Listener
{

    public abstract long getThreshold();

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.stopwatches.HistoryOfMyThread.Listener#onHistoryEnd(org.apache.commons.monitoring.stopwatches.HistoryOfMyThread,
     *      long)
     */
    public void onHistoryEnd( HistoryOfMyThread history, long elapsedTime )
    {
        long threshold = getThreshold();
        if ( elapsedTime > threshold )
        {
            exceed( elapsedTime, threshold, history );
        }
    }

    /**
     * @param elapsedTime
     * @param threshold TODO
     * @param history
     */
    public abstract void exceed( long elapsedTime, long threshold, HistoryOfMyThread history );

}
