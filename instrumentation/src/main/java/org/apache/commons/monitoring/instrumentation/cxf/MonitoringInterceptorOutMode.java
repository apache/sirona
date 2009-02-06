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

package org.apache.commons.monitoring.instrumentation.cxf;

import org.apache.commons.monitoring.StopWatch;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringInterceptorOutMode
    extends AbstractSoapInterceptor
{

    public MonitoringInterceptorOutMode()
    {
        super( Phase.SEND );
    }

    public void handleMessage( SoapMessage message )
        throws Fault
    {
        stop( message );
    }

    @Override
    public void handleFault( SoapMessage message )
    {
        stop( message );
    }

    protected final long stop( SoapMessage message )
    {
        StopWatch stopWatch = (StopWatch) message.getExchange().get( StopWatch.class );
        if ( stopWatch != null )
        {
            stopWatch.stop();
        }
        return stopWatch.getElapsedTime();
    }
}
