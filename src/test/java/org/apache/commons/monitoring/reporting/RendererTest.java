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

package org.apache.commons.monitoring.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.impl.SimpleCounter;
import org.apache.commons.monitoring.impl.SimpleGauge;
import org.apache.commons.monitoring.impl.SimpleMonitor;

public class RendererTest
    extends TestCase
{
    Collection<String> roles = Arrays.asList( new String[] { Monitor.CONCURRENCY, Monitor.PERFORMANCES } );

    Collection<Monitor> monitors;

    @Override
    protected void setUp()
        throws Exception
    {
        monitors = new LinkedList<Monitor>();
        Monitor m1 = new SimpleMonitor( "JsonRendererTest.setUp", "test", "reporting" );
        m1.setValue( new SimpleCounter(), Monitor.PERFORMANCES );
        m1.setValue( new SimpleGauge(), Monitor.CONCURRENCY );
        m1.getCounter( Monitor.PERFORMANCES ).add( 10 );
        monitors.add( m1 );

        Monitor m2 = new SimpleMonitor( "TestCase", "test", "junit" );
        m2.setValue( new SimpleCounter(), Monitor.PERFORMANCES );
        m2.setValue( new SimpleGauge(), Monitor.CONCURRENCY );
        m2.getGauge( Monitor.CONCURRENCY ).increment();
        monitors.add( m2 );
    }

    public void testRenderToJson()
        throws Exception
    {
        StringWriter out = new StringWriter();
        Renderer renderer = new JsonRenderer( new PrintWriter( out ), roles );
        renderer.render( monitors );
        assertEquals(
            "["
                + "{key:{name:\"JsonRendererTest.setUp\",category:\"test\",subsystem:\"reporting\"},"
                + "concurrency:{value:\"0\",min:\"0\",max:\"0\",mean:\"NaN\",stdDev:\"NaN\"},"
                + "performances:{value:\"10\",min:\"10\",max:\"10\",mean:\"0.0\",stdDev:\"NaN\",total:\"0\",hits:\"1\"}},"
                + "{key:{name:\"TestCase\",category:\"test\",subsystem:\"junit\"},"
                + "concurrency:{value:\"1\",min:\"1\",max:\"1\",mean:\"0.0\",stdDev:\"NaN\"},"
                + "performances:{value:\"0\",min:\"0\",max:\"0\",mean:\"NaN\",stdDev:\"NaN\",total:\"0\",hits:\"0\"}}"
                + "]", out.toString() );
    }

    public void testRenderToXml()
        throws Exception
    {
        StringWriter out = new StringWriter();
        Renderer renderer = new XmlRenderer( new PrintWriter( out ), roles );
        renderer.render( monitors );
        assertEquals(
            "<monitors>"
                + "<monitor name=\"JsonRendererTest.setUp\" category=\"test\" subsystem=\"reporting\">"
                + "<concurrency value=\"0\" min=\"0\" max=\"0\" mean=\"NaN\" stdDev=\"NaN\"/>"
                + "<performances value=\"10\" min=\"10\" max=\"10\" mean=\"0.0\" stdDev=\"NaN\" total=\"0\" hits=\"1\"/>"
                + "</monitor>"
                + "<monitor name=\"TestCase\" category=\"test\" subsystem=\"junit\">"
                + "<concurrency value=\"1\" min=\"1\" max=\"1\" mean=\"0.0\" stdDev=\"NaN\"/>"
                + "<performances value=\"0\" min=\"0\" max=\"0\" mean=\"NaN\" stdDev=\"NaN\" total=\"0\" hits=\"0\"/>"
                + "</monitor></monitors>", out.toString() );
    }
}
