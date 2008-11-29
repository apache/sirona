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

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class UnitTest
{
    @Test
    public void derived()
    {
        assertEquals( Unit.NANOS, Unit.HOUR.getPrimary() );
        assertEquals( Unit.NANOS, Unit.NANOS.getDerived( "ns" ) );
        assertEquals( Unit.MICROS, Unit.NANOS.getDerived( "µs" ) );
        assertEquals( Unit.MILLIS, Unit.NANOS.getDerived( "ms" ) );
        assertEquals( Unit.SECOND, Unit.NANOS.getDerived( "s" ) );
    }

    @Test
    public void scales()
    {
        assertEquals( 1L, Unit.NANOS.getScale() );
        assertEquals( 1000L, Unit.MICROS.getScale() );
        assertEquals( 1000000L, Unit.MILLIS.getScale() );
        assertEquals( 1000000000L, Unit.SECOND.getScale() );
    }
}
