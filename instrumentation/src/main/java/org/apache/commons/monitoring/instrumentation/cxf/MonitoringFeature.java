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

import org.apache.commons.monitoring.Repository;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * CXF feature to enable web service monitoring.
 * 
 * <pre>
 * &lt;jaxws:endpoint implementor=&quot;#myServiceBean&quot; address=&quot;/myService&quot; wsdlLocation=&quot;wsdl/myService.wsdl&quot;&gt;
 *   &lt;jaxws:features&gt;
 *     &lt;bean class=&quot;org.apache.commons.monitoring.instrumentation.cxf.MonitoringFeature&quot; /&gt;
 *   &lt;/jaxws:features&gt;
 * &lt;/jaxws:endpoint&gt;
 * </pre>
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringFeature
    extends AbstractFeature
{
    private Repository repository;

    private String category = "soap";

    private String domain;

    @Override
    protected void initializeProvider( InterceptorProvider provider, Bus bus )
    {
        MonitoringInInterceptor in = getMonitoringInInterceptor();
        in.setRepository( repository );
        in.setCategory( category );
        in.setDomain( domain );
        MonitoringOutInterceptor out = new MonitoringOutInterceptor();
        provider.getInInterceptors().add( in );
        provider.getInFaultInterceptors().add( in );
        provider.getOutInterceptors().add( out );
        provider.getOutFaultInterceptors().add( out );
    }

    protected MonitoringInInterceptor getMonitoringInInterceptor()
    {
        return new MonitoringInInterceptor();
    }

    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    public void setCategory( String category )
    {
        this.category = category;
    }

    public void setDomain( String domain )
    {
        this.domain = domain;
    }
}
