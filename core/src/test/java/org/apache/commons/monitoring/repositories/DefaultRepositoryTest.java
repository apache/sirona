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

package org.apache.commons.monitoring.repositories;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.repositories.DefaultRepository;
import org.junit.Test;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultRepositoryTest
{
    @Test
    public void categories()
        throws Exception
    {
        Repository repository = new DefaultRepository();
        Monitor foo = repository.getMonitor( "foo", "test" );
        repository.getMonitor( "bar", "junit" );
        repository.getMonitor( "just-a-name" );

        Set<String> categories = repository.getCategories();
        assertTrue( categories.contains( "test" ) );
        assertTrue( categories.contains( "junit" ) );
        assertTrue( categories.contains( Key.DEFAULT ) );
        assertEquals( 3, categories.size() );

        Collection<Monitor> monitors = repository.getMonitorsFromCategory( "test" );
        assertEquals( 1, monitors.size() );
        assertSame( foo, monitors.iterator().next() );
    }

    @Test
    public void subsystem()
        throws Exception
    {
        Repository repository = new DefaultRepository();
        Monitor foo = repository.getMonitor( "foo", "test", "test" );
        repository.getMonitor( "bar", "junit", "fake" );
        repository.getMonitor( "just-a-name" );

        Set<String> subsystems = repository.getSubSystems();
        assertTrue( subsystems.contains( "test" ) );
        assertTrue( subsystems.contains( "fake" ) );
        assertTrue( subsystems.contains( Key.DEFAULT ) );
        assertEquals( 3, subsystems.size() );

        Collection<Monitor> monitors = repository.getMonitorsFromSubSystem( "test" );
        assertEquals( 1, monitors.size() );
        assertSame( foo, monitors.iterator().next() );
    }

}
