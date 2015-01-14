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
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.util.ClassLoaders;

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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    private static final boolean METHOD_INVOCATION_ALLOWED =
        Configuration.is( Configuration.CONFIG_PROPERTY_PREFIX + "jmx.method.allowed", true );

    private static final Map<String, Class<?>> WRAPPERS = new HashMap<String, Class<?>>();

    static
    {
        for ( final Class<?> c : Arrays.<Class<?>>asList( Byte.class, //
                                                          Short.class, //
                                                          Integer.class, //
                                                          Long.class, //
                                                          Float.class, //
                                                          Double.class, //
                                                          Character.class, //
                                                          Boolean.class ) )
        {
            try
            {
                final Field f = c.getField( "TYPE" );
                Class<?> p = (Class<?>) f.get( null );
                WRAPPERS.put( p.getName(), c );
            }
            catch ( Exception e )
            {
                throw new AssertionError( e );
            }
        }
    }

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


    @POST
    @Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public JMXInvocationResult invoke( JMXInvocationRequest request )
    {

        if ( !METHOD_INVOCATION_ALLOWED )
        {
            throw new SironaException( "Method invocation not allowed" );
        }

        try
        {
            final ObjectName name =
                new ObjectName( new String( Base64.decodeBase64( request.getMbeanEncodedName() ) ) );
            final MBeanInfo info = server.getMBeanInfo( name );
            for ( final MBeanOperationInfo op : info.getOperations() )
            {
                if ( op.getName().equals( request.getOperationName() ) )
                {
                    final MBeanParameterInfo[] signature = op.getSignature();
                    final String[] sign = new String[signature.length];
                    for ( int i = 0; i < sign.length; i++ )
                    {
                        sign[i] = signature[i].getType();
                    }
                    final Object result = server.invoke( name, request.getOperationName(),
                                                         convertParams( signature, request.getParameters() ), sign );
                    return value( result );
                }
            }
        }
        catch ( final Exception e )
        {
            return new JMXInvocationResult().errorMessage( e.getMessage() );
        }

        return new JMXInvocationResult().errorMessage( request.getOperationName() + " not found" );
    }

    private Object[] convertParams( final MBeanParameterInfo[] signature, final List<String> params )
    {
        if ( params == null )
        {
            return null;
        }

        final Object[] convertedParams = new Object[signature.length];
        for ( int i = 0; i < signature.length; i++ )
        {
            if ( i < params.size() )
            {
                convertedParams[i] = convert( signature[i].getType(), params.get( i ) );
            }
            else
            {
                convertedParams[i] = null;
            }
        }
        return convertedParams;
    }

    public static Object convert( final String type, final String value )
    {
        try
        {
            if ( WRAPPERS.containsKey( type ) )
            {
                if ( type.equals( Character.TYPE.getName() ) )
                {
                    return value.charAt( 0 );
                }
                return tryStringConstructor( WRAPPERS.get( type ).getName(), value );
            }

            if ( type.equals( Character.class.getName() ) )
            {
                return value.charAt( 0 );
            }

            if ( Number.class.isAssignableFrom( ClassLoaders.current().loadClass( type ) ) )
            {
                return toNumber( value );
            }

            if ( value == null || value.equals( "null" ) )
            {
                return null;
            }

            return tryStringConstructor( type, value );
        }
        catch ( final Exception e )
        {
            throw new SironaException( e );
        }
    }


    private static Number toNumber( final String value )
        throws NumberFormatException
    {
        // first the user can force the conversion
        final char lastChar = Character.toLowerCase( value.charAt( value.length() - 1 ) );
        if ( lastChar == 'd' )
        {
            return Double.valueOf( value.substring( 0, value.length() - 1 ) );
        }
        if ( lastChar == 'l' )
        {
            return Long.valueOf( value.substring( 0, value.length() - 1 ) );
        }
        if ( lastChar == 'f' )
        {
            return Float.valueOf( value.substring( 0, value.length() - 1 ) );
        }

        // try all conversions in cascade until it works
        for ( final Class<?> clazz : new Class<?>[]{ Integer.class, Long.class, Double.class } )
        {
            try
            {
                return Number.class.cast( clazz.getMethod( "valueOf", String.class ).invoke( null, value ) );
            }
            catch ( final Exception e )
            {
                // no-op
            }
        }

        throw new SironaException( value + " is not a number" );
    }

    private static Object tryStringConstructor( String type, final String value )
        throws Exception
    {
        return ClassLoaders.current().loadClass( type ).getConstructor( String.class ).newInstance( value );
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
                value = e.getMessage();
            }
            list.add( new MBeanAttribute( attribute.getName(), //
                                          nullProtection( attribute.getType() ), //
                                          nullProtection( attribute.getDescription() ), //
                                          value == null ? "" : value.toString() ) );
        }
        return list;
    }

    public static String nullProtection( final String value )
    {
        return ( value == null ) ? EMPTY_STRING : value;
    }


    private static JMXInvocationResult value( final Object value )
    {
        try
        {
            if ( value == null )
            {
                return new JMXInvocationResult();
            }

            if ( value.getClass().isArray() )
            {
                final int length = Array.getLength( value );
                if ( length == 0 )
                {
                    return new JMXInvocationResult();
                }

                List<String> results = new ArrayList<String>( length );
                for ( int i = 0; i < length; i++ )
                {
                    Object o = Array.get( value, i );
                    if ( o != null )
                    {
                        results.add( o.toString() );
                    }
                }
                return new JMXInvocationResult().results( results );
            }

            if ( Collection.class.isInstance( value ) )
            {
                List<String> results = new ArrayList<String>();
                for ( final Object o : Collection.class.cast( value ) )
                {
                    if ( o != null )
                    {
                        results.add( o.toString() );
                    }
                }
                return new JMXInvocationResult().results( results );
            }

            if ( TabularData.class.isInstance( value ) )
            {
                final TabularData td = TabularData.class.cast( value );
                List<String> results = new ArrayList<String>();
                //final StringBuilder builder = new StringBuilder().append( "<table class=\"table table-condensed\">" );
                for ( final Object type : td.keySet() )
                {
                    final List<?> values = (List<?>) type;
                    final CompositeData data = td.get( values.toArray( new Object[values.size()] ) );
                    for ( final String k : data.getCompositeType().keySet() )
                    {
                        Object o = data.get( k );
                        if ( o != null )
                        {
                            results.add( o.toString() );
                        }
                    }
                }
                return new JMXInvocationResult().results( results );
            }

            if ( CompositeData.class.isInstance( value ) )
            {
                final CompositeData cd = CompositeData.class.cast( value );
                final Set<String> keys = cd.getCompositeType().keySet();

                Map<String, String> results = new HashMap<String, String>();

                for ( final String type : keys )
                {

                    Object o = cd.get( type );
                    results.put( type, o != null ? o.toString() : "" );
                }
                return new JMXInvocationResult().mapResult( results );
            }

            if ( Map.class.isInstance( value ) )
            {
                final Map<?, ?> map = Map.class.cast( value );

                Map<String, String> results = new HashMap<String, String>();

                for ( final Map.Entry<?, ?> entry : map.entrySet() )
                {
                    results.put( entry.getKey().toString(), entry.getValue().toString() );
                }
                return new JMXInvocationResult().mapResult( results );
            }

            return new JMXInvocationResult().results( Collections.singletonList( value.toString() ) );
        }
        catch ( final Exception e )
        {
            throw new SironaException( e.getMessage(), e );
        }
    }

}
