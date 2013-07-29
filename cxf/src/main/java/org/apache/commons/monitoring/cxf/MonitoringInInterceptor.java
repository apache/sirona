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

package org.apache.commons.monitoring.cxf;

import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;
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

import java.util.List;
import java.util.Map;

/**
 * A CXF Interceptor to apply monitoring on incoming messages.
 * <p/>
 * The monitor name is set based on message SOAPAction header (if set).
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringInInterceptor extends AbstractSoapInterceptor {
    private String category = "soap";

    public MonitoringInInterceptor() {
        super(Phase.READ);
    }

    public MonitoringInInterceptor(final String phase) {
        super(phase);
    }

    public void handleMessage(final SoapMessage message) throws Fault {
        final Monitor monitor = Repository.INSTANCE.getMonitor(getMonitorName(message), getMonitorCategory(message));
        StopWatch stopWatch = Repository.INSTANCE.start(monitor);
        message.getExchange().put(StopWatch.class, stopWatch);
    }

    /**
     * Detect the monitor name from incoming message
     * <p/>
     * May be overriden to use another Strategy to attach a Monitor to a SoapMessage
     *
     * @param message
     * @return
     */
    protected String getMonitorName(final SoapMessage message) {
        String soapAction = getSoapAction(message);
        return soapAction != null ? soapAction : "unknown";
    }

    /**
     * Detect the monitor category from incoming message
     * <p/>
     * May be overriden to use another Strategy to attach a Monitor to a SoapMessage
     *
     * @param message
     * @return
     */
    protected String getMonitorCategory(final SoapMessage message) {
        return category;
    }

    /**
     * Retrieve the SOAPAction header
     *
     * @param message the incoming message
     * @return the soapaction if detected
     * @see SoapActionInInterceptor
     */
    protected String getSoapAction(final SoapMessage message) {
        if (message.getVersion() instanceof Soap11) {
            final Map<String, List<String>> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));
            if (headers != null) {
                final List<String> sa = headers.get(SoapBindingConstants.SOAP_ACTION);
                if (sa != null && sa.size() > 0) {
                    String action = sa.get(0);
                    if (action.startsWith("\"")) {
                        action = action.substring(1, action.length() - 1);
                    }
                    return action;
                }
            }
        } else if (message.getVersion() instanceof Soap12) {
            final String ct = (String) message.get(Message.CONTENT_TYPE);

            if (ct != null) {
                int start = ct.indexOf("action=");
                if (start != -1) {
                    start += 7;
                    int end;
                    if (ct.charAt(start) == '\"') {
                        start += 1;
                        end = ct.indexOf('\"', start);
                    } else {
                        end = ct.indexOf(';', start);
                        if (end == -1) {
                            end = ct.length();
                        }
                    }
                    return ct.substring(start, end);
                }
            }
        }
        return null;
    }

    public void handleFault(final SoapMessage message) {
        message.getExchange().get(StopWatch.class).stop();
    }

    /**
     * @param category The monitoring category
     */
    public void setCategory(final String category) {
        this.category = category;
    }
}
