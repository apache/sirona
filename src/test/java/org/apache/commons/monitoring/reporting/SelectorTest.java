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

import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.repositories.DefaultRepository;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SelectorTest
    extends TestCase
{
    public void testSinglePath()
        throws Exception
    {
        Repository repository = new DefaultRepository();
        Selector selector = new Selector( "monitor/test/counter/performances" );

        repository.getMonitor( "test" ).getCounter( Monitor.PERFORMANCES ).add( 1234, Unit.NANOS );

        Object value = selector.select( repository );
        assertTrue( value instanceof Counter );
        Counter counter = (Counter) value;
        assertEquals( 1234L, counter.get() );
    }

    public void testCollectionPath()
        throws Exception
    {
        Repository repository = new DefaultRepository();
        Selector selector = new Selector( "monitors" );

        repository.getMonitor( "test" ).getCounter( Monitor.PERFORMANCES ).add( 1234, Unit.NANOS );

        Object value = selector.select( repository );
        assertTrue( value instanceof Collection );
        Collection collection = (Collection) value;
        assertEquals( 1, collection.size() );
        assertTrue( collection.iterator().next() instanceof Monitor );
    }

    public void testSinglePathGet()
        throws Exception
    {
        Repository repository = new DefaultRepository();
        Selector selector = new Selector( "monitor/test/counter/performances/" );

        repository.getMonitor( "test" ).getCounter( Monitor.PERFORMANCES ).add( 1234, Unit.NANOS );

        Object value = selector.select( repository );
        assertEquals( "1234", value.toString() );
    }

    public void testMultiplePath()
    {
        Repository repository = new DefaultRepository();
        Selector selector = new Selector( "monitors/counter/performances/" );

        repository.getMonitor( "one" ).getCounter( Monitor.PERFORMANCES ).add( 1234, Unit.NANOS );
        repository.getMonitor( "two" ).getCounter( Monitor.PERFORMANCES ).add( 5678, Unit.NANOS );

        Object value = selector.select( repository );
        assertTrue( value instanceof Collection );
        Collection values = (Collection) value;
        assertTrue( values.toString().contains( "1234" ) );
        assertTrue( values.toString().contains( "5678" ) );
    }
}
