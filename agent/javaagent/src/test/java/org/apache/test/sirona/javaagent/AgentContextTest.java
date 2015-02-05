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
package org.apache.test.sirona.javaagent;

import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class AgentContextTest
{
    @Test
    public void keyClass()
    {
        final Class<?> clazz = new AgentContext( "org.apache.test.sirona.javaagent.AgentContextTest.keyClass()", //
                                                 this, //
                                                 new InvocationListener[0], //
                                                 null ) //
            .keyAsClass();
        assertEquals( AgentContextTest.class, clazz );
    }

    @Test
    public void keyMethod()
        throws NoSuchMethodException
    {
        final Method mtd = new AgentContext( "org.apache.test.sirona.javaagent.AgentContextTest.keyMethod()", //
                                             this, //
                                             new InvocationListener[0], //
                                             null ) //
            .keyAsMethod();
        assertEquals( AgentContextTest.class.getMethod( "keyMethod" ), mtd );
    }
}
