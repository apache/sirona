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
package org.apache.sirona.cube;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AsyncHttpClientCube
    extends Cube
{

    private static final Logger LOGGER = Logger.getLogger( AsyncHttpClientCube.class.getName() );

    private AsyncHttpClient asyncHttpClient;

    public AsyncHttpClientCube( CubeBuilder cubeBuilder )
    {
        super( cubeBuilder );

        NettyAsyncHttpProviderConfig conf = new NettyAsyncHttpProviderConfig();
        conf.addProperty( NettyAsyncHttpProviderConfig.BOSS_EXECUTOR_SERVICE, Executors.newSingleThreadExecutor() );

        AsyncHttpClientConfig asyncHttpClientConfig =
            new AsyncHttpClientConfig.Builder().setAsyncHttpClientProviderConfig( conf ) //
                .setAllowPoolingConnection( true ) //
                .setAllowSslConnectionPool( true ) //
                .setConnectionTimeoutInMs( cubeBuilder.getConnectionTimeout() ) //
                .setRequestTimeoutInMs( cubeBuilder.getPostTimeout() ) //
                .setMaxRequestRetry( 1 ) //
                .setMaximumConnectionsPerHost( cubeBuilder.getDefaultMaxPerRoute() ) //
                .setMaximumConnectionsTotal( cubeBuilder.getMaxTotalConnections() ) //
                .build();
        this.asyncHttpClient = new AsyncHttpClient( asyncHttpClientConfig );

    }


    @Override
    public void doPostBytes( final byte[] bytes, final String className )
    {
        try
        {
            this.asyncHttpClient.preparePost( getConfig().getCollector() ) //
                .setBody( bytes ) //
                .addHeader( CONTENT_TYPE, APPLICATION_JAVA_OBJECT ) //
                .addHeader( X_SIRONA_CLASSNAME, className )//
                .execute( new AsyncHandler<String>()
                {

                    @Override
                    public void onThrowable( Throwable throwable )
                    {
                        if ( LOGGER.isLoggable( Level.FINE ) )
                        {
                            LOGGER.log( Level.FINE, "Can't post data to collector:" + throwable.getMessage(),
                                        throwable );
                        }
                        else
                        {
                            LOGGER.log( Level.WARNING, "Can't post data to collector: " + throwable.getMessage() );
                        }
                    }

                    @Override
                    public STATE onBodyPartReceived( HttpResponseBodyPart httpResponseBodyPart )
                        throws Exception
                    {
                        return STATE.ABORT;
                    }

                    @Override
                    public STATE onStatusReceived( HttpResponseStatus httpResponseStatus )
                        throws Exception
                    {
                        int statusCode = httpResponseStatus.getStatusCode();
                        // we don't care about the end of the content
                        if ( statusCode != 200 )
                        {
                            LOGGER.warning( "Pushed data but response code is: " + statusCode + //
                                                ", reason:" + httpResponseStatus.getStatusText() );
                        }

                        return STATE.ABORT;
                    }

                    @Override
                    public STATE onHeadersReceived( HttpResponseHeaders httpResponseHeaders )
                        throws Exception
                    {
                        return STATE.ABORT;
                    }

                    @Override
                    public String onCompleted()
                        throws Exception
                    {
                        return null;
                    }
                } );

        }
        catch ( IOException e )
        {
            if ( LOGGER.isLoggable( Level.FINE ) )
            {
                LOGGER.log( Level.FINE, "Can't post data to collector:" + e.getMessage(), e );
            }
            else
            {
                LOGGER.log( Level.WARNING, "Can't post data to collector: " + e.getMessage() );
            }
        }
    }
}
