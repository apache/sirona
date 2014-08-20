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

package org.apache.sirona.reporting.web.threads;

import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * @since 0.3
 */
@Path( "/threads" )
public class ThreadsReports
{


    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Collection<ThreadInfo> all()
    {
        return listThreads();
    }

    @GET
    @Path( "/{threadencodedName}" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public ThreadDump getThread( @PathParam( "threadencodedName" ) String threadencodedName )
    {
        Thread thread = findThread( threadencodedName );
        if ( thread == null )
        {
            return null;
        }

        return new ThreadDump( thread.getState().name(), dump( thread ) );

    }

    private static Thread findThread( final String threadencodedName )
    {
        int count = Thread.activeCount();
        final Thread[] threads = new Thread[count];
        count = Thread.enumerate( threads );

        for ( int i = 0; i < count; i++ )
        {
            if ( Base64.encodeBase64URLSafeString( threads[i].getName().getBytes() ).equals( threadencodedName ) )
            {
                return threads[i];
            }
        }

        return null;
    }

    private static String dump( final Thread thread )
    {
        final StackTraceElement[] stack = thread.getStackTrace();
        final StringBuilder builder = new StringBuilder();
        for ( final StackTraceElement element : stack )
        {
            builder.append( element.toString() ).append( "\n" ); // toString method is fine
        }
        return builder.toString();
    }

    private static Collection<ThreadInfo> listThreads()
    {
        final Set<ThreadInfo> out = new TreeSet<ThreadInfo>( ThreadInfo.COMPARATOR );
        int count = Thread.activeCount();
        final Thread[] threads = new Thread[count];
        count = Thread.enumerate( threads );
        for ( int i = 0; i < count; i++ )
        {
            final String name = threads[i].getName();
            out.add( new ThreadInfo( name, Base64.encodeBase64URLSafeString( name.getBytes() ) ) );
        }

        return out;
    }

}
