///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.commons.monitoring.support.jaxws;
//
//import static javax.xml.ws.handler.MessageContext.WSDL_OPERATION;
//import static javax.xml.ws.handler.MessageContext.WSDL_SERVICE;
//
//import java.util.Set;
//
//import javax.xml.namespace.QName;
//import javax.xml.ws.handler.MessageContext;
//import javax.xml.ws.handler.soap.SOAPHandler;
//import javax.xml.ws.handler.soap.SOAPMessageContext;
//
//import org.apache.commons.monitoring.Monitor;
//import org.apache.commons.monitoring.Monitoring;
//import org.apache.commons.monitoring.Repository;
//import org.apache.commons.monitoring.StopWatch;
//import org.apache.commons.monitoring.Unit;
//
///**
// * JAX-WS handler to monitor web services performances.
// * <p>
// * WARNING : experimental code
// *
// * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
// */
//public class PerformancesHandler
//    implements SOAPHandler<SOAPMessageContext>
//{
//    private static final String STOPWATCH = "org.apache.commons.monitoring.support.jaxws.STOPWATCH";
//
//    private static final String MONITOR = "org.apache.commons.monitoring.support.jaxws.MONITOR";
//
//    private Repository repository = Monitoring.getRepository();
//
//    private String category = "jax-ws";
//
//    private String subsystem;
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see javax.xml.ws.handler.soap.SOAPHandler#getHeaders()
//     */
//    public Set<QName> getHeaders()
//    {
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see javax.xml.ws.handler.Handler#handleMessage(javax.xml.ws.handler.MessageContext)
//     */
//    public boolean handleMessage( SOAPMessageContext context )
//    {
//        Monitor monitor = repository.getMonitor( getMonitorName( context ), category, subsystem );
//        context.put( MONITOR, repository.start( monitor ) );
//        context.put( STOPWATCH, repository.start( monitor ) );
//        return true;
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see javax.xml.ws.handler.Handler#close(javax.xml.ws.handler.MessageContext)
//     */
//    public void close( MessageContext context )
//    {
//        StopWatch stopWatch = (StopWatch) context.get( STOPWATCH );
//        if ( stopWatch != null )
//        {
//            stopWatch.stop();
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see javax.xml.ws.handler.Handler#handleFault(javax.xml.ws.handler.MessageContext)
//     */
//    public boolean handleFault( SOAPMessageContext context )
//    {
//        Monitor monitor = (Monitor) context.get( MONITOR );
//        StopWatch stopWatch = (StopWatch) context.get( STOPWATCH );
//        if ( stopWatch != null && monitor != null )
//        {
//            monitor.getCounter( Monitor.FAILURES ).add( stopWatch.getElapsedTime(), Unit.NANOS );
//        }
//        return true;
//    }
//
//    /**
//     * Build the monitor name for the incoming message
//     *
//     * @param context the JAX-WS MessageContext
//     * @return the monitor name to use
//     */
//    protected String getMonitorName( SOAPMessageContext context )
//    {
//        StringBuilder name = new StringBuilder();
//        name.append( context.get( WSDL_SERVICE ) );
//        name.append( "." );
//        name.append( context.get( WSDL_OPERATION ) );
//        return name.toString();
//    }
//
//    /**
//     * @param repository the repository to set
//     */
//    public void setRepository( Repository repository )
//    {
//        this.repository = repository;
//    }
//
//    /**
//     * @param category the category to set
//     */
//    public void setCategory( String category )
//    {
//        this.category = category;
//    }
//
//    /**
//     * @param subsystem the subsystem to set
//     */
//    public void setSubsystem( String subsystem )
//    {
//        this.subsystem = subsystem;
//    }
//
//}
