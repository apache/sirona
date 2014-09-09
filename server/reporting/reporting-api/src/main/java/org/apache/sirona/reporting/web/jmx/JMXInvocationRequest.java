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

package org.apache.sirona.reporting.web.jmx;

import java.io.Serializable;
import java.util.List;

/**
 * @since 0.3
 */
public class JMXInvocationRequest
    implements Serializable
{

    private String mbeanEncodedName;

    private String operationName;

    private List<String> parameters;

    public JMXInvocationRequest()
    {
        // no op
    }

    public JMXInvocationRequest( String mbeanEncodedName, String operationName, List<String> parameters )
    {
        this.mbeanEncodedName = mbeanEncodedName;
        this.operationName = operationName;
        this.parameters = parameters;
    }

    public String getMbeanEncodedName()
    {
        return mbeanEncodedName;
    }

    public void setMbeanEncodedName( String mbeanEncodedName )
    {
        this.mbeanEncodedName = mbeanEncodedName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public void setParameters( List<String> parameters )
    {
        this.parameters = parameters;
    }

    @Override
    public String toString()
    {
        return "JMXInvocationRequest{" +
            "mbeanEncodedName='" + mbeanEncodedName + '\'' +
            ", operationName='" + operationName + '\'' +
            ", parameters=" + parameters +
            '}';
    }
}
