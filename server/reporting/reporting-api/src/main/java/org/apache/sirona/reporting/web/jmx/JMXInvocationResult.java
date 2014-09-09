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
import java.util.Map;

/**
 * @since 0.3
 */
public class JMXInvocationResult
    implements Serializable
{

    private String errorMessage;

    private List<String> results;

    private Map<String, String> mapResult;

    public JMXInvocationResult()
    {
        // no op
    }

    public JMXInvocationResult( String errorMessage, List<String> results, Map<String, String> mapResult )
    {
        this.errorMessage = errorMessage;
        this.results = results;
        this.mapResult = mapResult;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    public JMXInvocationResult errorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
        return this;
    }

    public List<String> getResults()
    {
        return results;
    }

    public void setResults( List<String> results )
    {
        this.results = results;
    }

    public JMXInvocationResult results( List<String> results )
    {
        this.results = results;
        return this;
    }

    public Map<String, String> getMapResult()
    {
        return mapResult;
    }

    public void setMapResult( Map<String, String> mapResult )
    {
        this.mapResult = mapResult;
    }

    public JMXInvocationResult mapResult( Map<String, String> mapResult )
    {
        this.mapResult = mapResult;
        return this;
    }

    @Override
    public String toString()
    {
        return "JMXInvocationResult{" +
            "errorMessage='" + errorMessage + '\'' +
            ", results=" + results +
            ", mapResult=" + mapResult +
            '}';
    }
}
