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

import java.util.List;
import java.util.Map;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringInInterceptor
    extends AbstractSoapInterceptor
{
    private Repository repository;

    private String category = "soap";

    private String domain;

    public MonitoringInInterceptor()
    {
        super( Phase.READ );
    }

    public void handleMessage( SoapMessage message )
        throws Fault
    {
        Monitor monitor = getMonitor( message );
        StopWatch stopWatch = repository.start( monitor );
        message.getExchange().put( StopWatch.class, stopWatch );
    }

    /**
     * Select a Monitor for the incoming message.
     * <p>
     * May be overriden to use another Strategy to attach a Monitor to a SoapMessage
     * 
     * @param message Soap IN message
     * @return Monitor
     */
    protected Monitor getMonitor( SoapMessage message )
    {
        return repository.getMonitor( getSoapAction( message ), category, domain );
    }

    /**
     * Retrieve the SOAPAction header
     * 
     * @see SoapActionInInterceptor
     * @param message the incoming message
     * @return the soapaction if detected
     */
    protected String getSoapAction( SoapMessage message )
    {
        if ( message.getVersion() instanceof Soap11 )
        {
            Map<String, List<String>> headers = CastUtils.cast( (Map) message.get( Message.PROTOCOL_HEADERS ) );
            if ( headers != null )
            {
                List<String> sa = headers.get( SoapBindingConstants.SOAP_ACTION );
                if ( sa != null && sa.size() > 0 )
                {
                    String action = sa.get( 0 );
                    if ( action.startsWith( "\"" ) )
                    {
                        action = action.substring( 1, action.length() - 1 );
                    }
                    return action;
                }
            }
        }
        else if ( message.getVersion() instanceof Soap12 )
        {
            String ct = (String) message.get( Message.CONTENT_TYPE );

            if ( ct != null )
            {
                int start = ct.indexOf( "action=" );
                if ( start != -1 )
                {
                    start += 7;
                    int end;
                    if ( ct.charAt( start ) == '\"' )
                    {
                        start += 1;
                        end = ct.indexOf( '\"', start );
                    }
                    else
                    {
                        end = ct.indexOf( ';', start );
                        if ( end == -1 )
                        {
                            end = ct.length();
                        }
                    }
                    return ct.substring( start, end );
                }
            }
        }
        return "unknown";
    }

    public void handleFault( SoapMessage message )
    {
        StopWatch stopWatch = (StopWatch) message.getExchange().get( StopWatch.class );
        stopWatch.stop();
    }

    /**
     * @param repository The monitoring repository
     */
    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    /**
     * @param category The monitoring category
     */
    public void setCategory( String category )
    {
        this.category = category;
    }

    /**
     * @param domain The monitoring domain
     */
    public void setDomain( String domain )
    {
        this.domain = domain;
    }
}
