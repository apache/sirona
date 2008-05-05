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

package org.apache.commons.monitoring.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Spring-aop implementation of PerformanceInterceptor.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class AopPerformanceInterceptor
    extends AbstractPerformanceInterceptor<MethodInvocation>
    implements MethodInterceptor
{

    /**
     * {@inheritDoc}
     *
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke( MethodInvocation invocation )
        throws Throwable
    {
        return doInvoke( invocation );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.aop.AbstractPerformanceInterceptor#getMonitorName(java.lang.Object)
     */
    @Override
    protected String getMonitorName( MethodInvocation invocation )
    {
        return getMonitorName( invocation.getMethod() );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.aop.AbstractPerformanceInterceptor#proceed(java.lang.Object)
     */
    @Override
    protected Object proceed( MethodInvocation invocation )
    throws Throwable
    {
        return invocation.proceed();
    }

}