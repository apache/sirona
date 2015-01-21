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
package org.apache.sirona.gauges.jvm;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class CPUGauge
    implements Gauge
{
    public static final Role CPU = new Role( "CPU", Unit.UNARY );

    private static final OperatingSystemMXBean SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();

    private Method method;

    public CPUGauge()
    {
        // get method if 1.6
        try
        {
            method = OperatingSystemMXBean.class.getMethod( "getSystemLoadAverage", null );
        }
        catch ( NoSuchMethodException e )
        {
            //
        }

    }

    @Override
    public Role role()
    {
        return CPU;
    }

    @Override
    public double value()
    {
        // 1.5 compat
        //return SYSTEM_MX_BEAN.getSystemLoadAverage();
        if ( this.method == null )
        {
            return -1;
        }
        try
        {
            return (Double) method.invoke( SYSTEM_MX_BEAN, null );
        }
        catch ( Exception e )
        {
            return -1;
        }
    }
}
