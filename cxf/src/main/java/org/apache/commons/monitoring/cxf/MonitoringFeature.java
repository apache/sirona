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

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * CXF feature to enable web service monitoring.
 * <p/>
 * <pre>
 * &lt;jaxws:endpoint implementor=&quot;#myServiceBean&quot; address=&quot;/myService&quot; wsdlLocation=&quot;wsdl/myService.wsdl&quot;&gt;
 *   &lt;jaxws:features&gt;
 *     &lt;bean class=&quot;org.apache.commons.monitoring.cxf.MonitoringFeature&quot; /&gt;
 *   &lt;/jaxws:features&gt;
 * &lt;/jaxws:endpoint&gt;
 * </pre>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringFeature extends AbstractFeature {
    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        final MonitoringInInterceptor in = getMonitoringInInterceptor();
        final MonitoringOutInterceptor out = new MonitoringOutInterceptor();
        provider.getInInterceptors().add(in);
        provider.getInFaultInterceptors().add(in);
        provider.getOutInterceptors().add(out);
        provider.getOutFaultInterceptors().add(out);
    }

    protected MonitoringInInterceptor getMonitoringInInterceptor() {
        return new MonitoringInInterceptor();
    }
}
