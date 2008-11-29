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

package org.apache.commons.monitoring.instrumentation.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Spring-aop implementation of PerformanceInterceptor.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class JavaProxyPerformanceInterceptor
    extends AbstractPerformanceInterceptor<Invocation>
{

    /**
     * Create a java.lang.reflect.Proxy around the target object and monitor it's methods execution.
     */
    public Object proxy( final Object target, Class<?>... interfaces )
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl == null )
        {
            cl = getClass().getClassLoader();
        }
        return Proxy.newProxyInstance( cl, interfaces, new InvocationHandler()
        {
            public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable
            {
                return doInvoke( new Invocation( target, method, args ) );
            }
        } );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.instrument.AbstractPerformanceInterceptor#getMonitorName(java.lang.Object)
     */
    @Override
    protected String getMonitorName( Invocation invocation )
    {
        return getMonitorName( invocation.method );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.instrument.AbstractPerformanceInterceptor#proceed(java.lang.Object)
     */
    @Override
    protected Object proceed( Invocation invocation )
        throws Throwable
    {
        return invocation.method.invoke( null, invocation.args );
    }
}

class Invocation
{
    public Invocation( Object proxy, Method method, Object[] args )
    {
        super();
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public Object proxy;

    public Method method;

    public Object[] args;
}