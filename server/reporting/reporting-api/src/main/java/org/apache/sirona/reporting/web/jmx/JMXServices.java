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
import org.apache.sirona.SironaException;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 0.3
 */
@Path( "/jmx" )
public class JMXServices
{
    private final MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();

    private static final String EMPTY_STRING = "";

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
    public MBeanInformations find( @PathParam( "encodedName" ) String encodedName )
        throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
        ReflectionException
    {
        final ObjectName name = new ObjectName( new String( Base64.decodeBase64( encodedName ) ) );

/*
    .set("objectname", name.toString())
    .set("objectnameHash", Base64.encodeBase64URLSafeString(name.toString().getBytes()))
    .set("classname", info.getClassName())
    .set("description", value(info.getDescription()))
    .set("attributes", attributes(name, info))
    .set("operations", operations(info))
    */

        final MBeanInfo info = server.getMBeanInfo( name );

        MBeanInformations mBeanInformations = new MBeanInformations( name.toString(), //
                                                                     Base64.encodeBase64URLSafeString(
                                                                         name.toString().getBytes() ), //
                                                                     info.getClassName(), //
                                                                     info.getDescription(), //
                                                                     new ArrayList<MBeanAttribute>(
                                                                         attributes( name, info ) ), //
                                                                     new ArrayList<MBeanOperation>(
                                                                         operations( info ) ) );

        return mBeanInformations;

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

    private Collection<MBeanOperation> operations( final MBeanInfo info )
    {
        final Collection<MBeanOperation> operations = new LinkedList<MBeanOperation>();
        for ( final MBeanOperationInfo operationInfo : info.getOperations() )
        {
            final MBeanOperation mBeanOperation =
                new MBeanOperation( operationInfo.getName(), operationInfo.getReturnType() );
            for ( final MBeanParameterInfo param : operationInfo.getSignature() )
            {
                mBeanOperation.getParameters().add( new MBeanParameter( param.getName(), param.getType() ) );
            }
            operations.add( mBeanOperation );
        }
        return operations;
    }

    // FIXME this html stuff here is just weird!!!
    private Collection<MBeanAttribute> attributes( final ObjectName name, final MBeanInfo info )
    {
        final Collection<MBeanAttribute> list = new LinkedList<MBeanAttribute>();
        for ( final MBeanAttributeInfo attribute : info.getAttributes() )
        {
            Object value;
            try
            {
                value = server.getAttribute( name, attribute.getName() );
            }
            catch ( final Exception e )
            {
                value = "<div class=\"alert-error\">" + e.getMessage() + "</div>";
            }
            list.add( new MBeanAttribute( attribute.getName(), nullProtection( attribute.getType() ),
                                          nullProtection( attribute.getDescription() ), value( value ) ) );
        }
        return list;
    }

    public static String nullProtection( final String value )
    {
        if ( value == null )
        {
            return EMPTY_STRING;
        }
        return value;
    }

    // FIXME this html stuff here is just weird!!!
    private static String value( final Object value )
    {
        try
        {
            if ( value == null )
            {
                return nullProtection( null );
            }

            if ( value.getClass().isArray() )
            {
                final int length = Array.getLength( value );
                if ( length == 0 )
                {
                    return "";
                }

                final StringBuilder builder = new StringBuilder().append( "<ul>" );
                for ( int i = 0; i < length; i++ )
                {
                    builder.append( "<li>" ).append( value( Array.get( value, i ) ) ).append( "</li>" );
                }
                builder.append( "</ul>" );
                return builder.toString();
            }

            if ( Collection.class.isInstance( value ) )
            {
                final StringBuilder builder = new StringBuilder().append( "<ul>" );
                for ( final Object o : Collection.class.cast( value ) )
                {
                    builder.append( "<li>" ).append( value( o ) ).append( "</li>" );
                }
                builder.append( "</ul>" );
                return builder.toString();
            }

            if ( TabularData.class.isInstance( value ) )
            {
                final TabularData td = TabularData.class.cast( value );
                final StringBuilder builder = new StringBuilder().append( "<table class=\"table table-condensed\">" );
                for ( final Object type : td.keySet() )
                {
                    final List<?> values = (List<?>) type;
                    final CompositeData data = td.get( values.toArray( new Object[values.size()] ) );
                    builder.append( "<tr>" );
                    final Set<String> dataKeys = data.getCompositeType().keySet();
                    for ( final String k : data.getCompositeType().keySet() )
                    {
                        builder.append( "<td>" ).append( value( data.get( k ) ) ).append( "</td>" );
                    }
                    builder.append( "</tr>" );
                }
                builder.append( "</table>" );

                return builder.toString();
            }

            if ( CompositeData.class.isInstance( value ) )
            {
                final CompositeData cd = CompositeData.class.cast( value );
                final Set<String> keys = cd.getCompositeType().keySet();

                final StringBuilder builder = new StringBuilder().append( "<table class=\"table table-condensed\">" );
                for ( final String type : keys )
                {
                    builder.append( "<tr><td>" ).append( type ).append( "</td><td>" ).append(
                        value( cd.get( type ) ) ).append( "</td></tr>" );
                }
                builder.append( "</table>" );

                return builder.toString();

            }

            if ( Map.class.isInstance( value ) )
            {
                final Map<?, ?> map = Map.class.cast( value );

                final StringBuilder builder = new StringBuilder().append( "<table class=\"table table-condensed\">" );
                for ( final Map.Entry<?, ?> entry : map.entrySet() )
                {
                    builder.append( "<tr><tr>" ).append( value( entry.getKey() ) ).append( "</td><td>" ).append(
                        value( entry.getValue() ) ).append( "</td></tr>" );
                }
                builder.append( "</table>" );

                return builder.toString();

            }

            return value.toString();
        }
        catch ( final Exception e )
        {
            throw new SironaException( e );
        }
    }

}
