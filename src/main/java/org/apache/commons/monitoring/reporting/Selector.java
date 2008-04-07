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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.monitoring.Monitor.Key;

/**
 * use a REST-style path to select a group of statValues. For example, to get
 * the mean performance for monitors in category "services"
 * <tt>/monitorsFromCategory/services/counter/performances/mean</tt>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class Selector
{

    /**
     *
     */
    private static final String SEP = "/";
    private String path;

    /**
     * Constructor
     *
     * @param path
     */
    public Selector( String path )
    {
        super();
        this.path = path;
    }

    /**
     *
     */
    public Object select( Object resource )
    {
        Stack<String> stack = new Stack<String>();
        StringTokenizer tokenizer = new StringTokenizer( path, SEP, true );
        String previous = null;
        while( tokenizer.hasMoreTokens() )
        {
            String next = tokenizer.nextToken();
            if ( SEP.equals( next ) )
            {
                if ( SEP.equals( previous ) )
                {
                    stack.push( Key.DEFAULT );
                }
            }
            else
            {
                stack.push( next );
            }
            previous = next;
        }
        if ( path.endsWith( SEP ) )
        {
            stack.push( Key.DEFAULT );
        }
        Collections.reverse( stack );

        return select( resource, stack );
    }

    @SuppressWarnings( "unchecked" )
    protected Object select( Object resource, Stack<String> path )
        throws IllegalArgumentException
    {
        String element = path.pop();
        Method accessor = getAccessor( resource, element );
        int i = accessor.getParameterTypes().length;
        Object[] args = new Object[i];
        if ( path.size() < i )
        {
            throw new IllegalArgumentException( "No enough arguments to call " + accessor );
        }
        for ( int j = 0; j < i; j++ )
        {
            args[j] = path.pop();
        }
        try
        {
            resource = accessor.invoke( resource, args );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Failed to invoke " + accessor );
        }

        if ( resource instanceof Collection && !path.isEmpty() )
        {
            Collection input = (Collection) resource;
            Collection result = new ArrayList( input.size() );
            for ( Iterator iterator = input.iterator(); iterator.hasNext(); )
            {
                Object sub = (Object) iterator.next();
                Stack<String> branch = new Stack<String>();
                branch.addAll( path );
                result.add( select( sub, branch ) );
            }
            path.clear();
            resource = result;
        }

        if ( !path.isEmpty() )
        {
            resource = select( resource, path );
        }
        return resource;
    }

    /**
     * Retrieve a getter method that only requires String parameters. When multiple methods
     * match, the on with the most parameters is returned, for example
     * getMonitor( String, String, String ) in preference to getMonitor( String )
     *
     * @param resource
     * @param name
     * @return
     */
    @SuppressWarnings( "unchecked" )
    protected Method getAccessor( Object resource, String name )
    {
        String accessor = "get";
        if ( name.length() > 0 )
        {
            accessor += Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );
        }
        Method[] methods = resource.getClass().getMethods();
        Method bestMatch = null;
        for ( int i = 0; i < methods.length; i++ )
        {
            Method method = methods[i];
            if ( method.getName().equals( accessor ) )
            {
                Class[] parameters = method.getParameterTypes();
                boolean stringsOnly = true;
                for ( int j = 0; j < parameters.length; j++ )
                {
                    if ( parameters[j] != String.class )
                    {
                        stringsOnly = false;
                        break;
                    }
                }
                if ( stringsOnly )
                {
                    if ( bestMatch == null || bestMatch.getParameterTypes().length < method.getParameterTypes().length )
                    {
                        bestMatch = method;
                    }
                }
            }
        }
        if ( bestMatch == null )
        {
            throw new IllegalArgumentException( "No accessor for " + name + " on resource " + resource );
        }
        return bestMatch;
    }
}
