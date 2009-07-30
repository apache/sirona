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

import static org.apache.commons.monitoring.Monitor.CONCURRENCY;
import static org.apache.commons.monitoring.Monitor.FAILURES;
import static org.apache.commons.monitoring.Unit.UNARY;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.repositories.DefaultRepository;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class RendererTest
{
    private Repository repository;

    @Before
    public void setup()
    {
        repository = new DefaultRepository();
        repository.getMonitor( "RendererTest", "unit", "test" ).getCounter( FAILURES ).add( 1.0 );
        repository.getMonitor( "RendererTest", "unit", "test" ).getGauge( CONCURRENCY ).increment( UNARY );
    }

    @Test
    public void renderToXML()
        throws Exception
    {
        StringWriter out = new StringWriter();
        Visitor v = new XmlRenderer( new VisitorConfigurationSupport(), new PrintWriter( out ) );
        repository.accept( v );

        Reader expected = new InputStreamReader( getClass().getResourceAsStream( "RendererTest.xml" ) );
        XMLAssert.assertXMLEqual( expected, new StringReader( out.toString() ) );
    }

    @Test
    public void renderToJSON()
        throws Exception
    {
        StringWriter out = new StringWriter();
        Visitor v = new JsonRenderer( new VisitorConfigurationSupport(), new PrintWriter( out ) );
        repository.accept( v );

        System.out.println( out.toString() );
        // JSON Testing framework
    }
}
