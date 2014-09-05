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

import org.apache.commons.codec.binary.Base64;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @since 0.3
 */
@Path( "/jmx" )
public class TheJmxServices
{
    protected final MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();

    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public JMXNode root()
        throws IOException
    {
        JMXNode jmxNode = buildJmxTree();
        return jmxNode;
    }

    @GET
    @Path( "/{encodedName}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public MBeanInfo find(@PathParam( "encodedName" ) String encodedName )
        throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
        ReflectionException
    {
        final ObjectName name = new ObjectName( new String( Base64.decodeBase64( encodedName ) ) );
        final MBeanInfo info = server.getMBeanInfo( name );
        return info;
    }


    private JMXNode buildJmxTree()
        throws IOException
    {
        final JMXNode root = new JMXNode( "/" );

        for ( final ObjectInstance instance : server.queryMBeans( null, null ) )
        {
            final ObjectName objectName = instance.getObjectName();
            JMXNode.addNode( root, objectName.getDomain(), objectName.getKeyPropertyListString() );
        }

        return root;
    }

}
