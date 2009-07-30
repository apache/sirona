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

package org.apache.commons.monitoring.reporting;

import static org.junit.Assert.assertTrue;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.repositories.DefaultRepository;
import org.junit.Test;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class AbstractPeriodicLoggerTest
{
    private int count = 0;

    @Test
    public void schedule()
        throws Exception
    {
        AbstractPeriodicLogger logger = new AbstractPeriodicLogger( 10, new DefaultRepository() ) {
            protected void log( Repository repositoryForPeriod )
            {
                count++;
            }
        };
        logger.init();
        Thread.sleep( 110 );
        logger.stop();
        assertTrue( count >= 10 );
    }
}
