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

package org.apache.commons.monitoring;

import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ExecutionStackTest
    extends TestCase
{
    public void testExcution()
        throws Exception
    {
        StopWatch s1 = new StopWatch( null );
        ExecutionStack.push( s1 );
        StopWatch s2 = new StopWatch( null );
        ExecutionStack.push( s2 );
        StopWatch s3 = new StopWatch( null );
        ExecutionStack.push( s3 );

        ExecutionStack.pause();
        assertTrue( s1.isPaused() );
        assertTrue( s2.isPaused() );
        assertTrue( s3.isPaused() );

        ExecutionStack.resume();
        assertTrue( ! s1.isPaused() );
        assertTrue( ! s2.isPaused() );
        assertTrue( ! s3.isPaused() );

        ExecutionStack.clear();
    }
}
