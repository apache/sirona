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
package org.apache.sirona.javaagent.listener;

import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class AgentContextTest
{
    @Test
    public void keyClass()
    {
        final Class<?> clazz = new AgentContext( "org.apache.sirona.javaagent.listener.AgentContextTest.keyClass()", //
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
        final Method mtd = new AgentContext( "org.apache.sirona.javaagent.listener.AgentContextTest.keyMethod()", //
                                             this, //
                                             new InvocationListener[0], //
                                             null ) //
            .keyAsMethod();
        assertEquals( AgentContextTest.class.getMethod( "keyMethod" ), mtd );
    }

    @Test
    public void extract_class_method_names()
    {
        //org.apache.test.sirona.javaagent.App.foo()
        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", //
                             PathTrackingListener.extractClassName( "org.apache.test.sirona.javaagent.App.foo()" ) );

        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", //
                             PathTrackingListener.extractClassName(
                                 "org.apache.test.sirona.javaagent.App.pub(java.lang.String)" ) );

        Assert.assertEquals( "App", //
                             PathTrackingListener.extractClassName( "App.foo()" ) );

        Assert.assertEquals( "org.App", //
                             PathTrackingListener.extractClassName( "org.App.foo()" ) );

        //org.apache.test.sirona.javaagent.App.pub(java.lang.String)
        Assert.assertEquals( "pub(java.lang.String)", //
                             PathTrackingListener.extractMethodName(
                                 "org.apache.test.sirona.javaagent.App.pub(java.lang.String)" ) );

        Assert.assertEquals( "foo()", //
                             PathTrackingListener.extractMethodName( "org.apache.test.sirona.javaagent.App.foo()" ) );

        Assert.assertEquals( "foo()", //
                             PathTrackingListener.extractMethodName( "App.foo()" ) );

        Assert.assertEquals( "foo()", //
                             PathTrackingListener.extractMethodName( "org.App.foo()" ) );
    }
}
