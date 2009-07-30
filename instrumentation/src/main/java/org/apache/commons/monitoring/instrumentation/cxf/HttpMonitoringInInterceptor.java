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

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

/**
 * A variant of MonitoringInInterceptor dedicated to web service endpoint based on HttpServlet - most commonly used, but
 * not required.
 * <p>
 * When no SOAPAction Header is set, the monitor name is extracted from servlet PathInfo
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HttpMonitoringInInterceptor
    extends MonitoringInInterceptor
{
    public HttpMonitoringInInterceptor()
    {
        super();
    }

    public HttpMonitoringInInterceptor( String phase )
    {
        super( phase );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.instrumentation.cxf.MonitoringInInterceptor#getMonitorName(org.apache.cxf.binding.soap.SoapMessage)
     */
    @Override
    protected String getMonitorName( SoapMessage message )
    {
        String soapAction = getSoapAction( message );
        if ( soapAction != null )
        {
            return soapAction;
        }
        HttpServletRequest request = (HttpServletRequest) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getPathInfo();
    }
}
