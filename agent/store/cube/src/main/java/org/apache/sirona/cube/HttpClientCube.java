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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class HttpClientCube
    extends Cube
{

    private static final Logger LOGGER = Logger.getLogger( HttpClientCube.class.getName() );

    private HttpClient httpclient;

    private CloseableHttpAsyncClient closeableHttpAsyncClient;

    private RequestConfig requestConfig;

    public HttpClientCube( CubeBuilder cubeBuilder )
    {
        super( cubeBuilder );
        try
        {

            requestConfig = RequestConfig.custom() //
                .setSocketTimeout( cubeBuilder.getPostTimeout() ) //
                .setConnectTimeout( cubeBuilder.getConnectionTimeout() ) //
                .setConnectionRequestTimeout( cubeBuilder.getConnectionRequestTimeout() ) //
                .build();

            if ( cubeBuilder.isUseAsync() )
            {
                IOReactorConfig ioReactorConfig =
                    IOReactorConfig.custom().setIoThreadCount( cubeBuilder.getAsyncIoThreadCount() ) //
                        .setConnectTimeout( cubeBuilder.getConnectionTimeout() ) //
                        .setSoTimeout( cubeBuilder.getPostTimeout() ) //
                        .setConnectTimeout( cubeBuilder.getConnectionTimeout() ) //
                        .build();

                ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor( ioReactorConfig );
                NHttpClientConnectionManager manager = new PoolingNHttpClientConnectionManager( ioReactor );
                closeableHttpAsyncClient = HttpAsyncClients.custom() //
                    .setConnectionManager( manager ) //
                    .setMaxConnPerRoute( cubeBuilder.getDefaultMaxPerRoute() ) //
                    .setMaxConnTotal( cubeBuilder.getMaxTotalConnections() ) //
                    .setDefaultIOReactorConfig( ioReactorConfig ) //
                    .build();
                closeableHttpAsyncClient.start();
            }
            else
            {

                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

                connectionManager.setMaxTotal( cubeBuilder.getMaxTotalConnections() );

                connectionManager.setDefaultMaxPerRoute( cubeBuilder.getDefaultMaxPerRoute() );

                httpclient = HttpClientBuilder.create().setConnectionManager( connectionManager ).build();

            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    @Override
    public void doPostBytes( final byte[] bytes, final String className )
    {

        try
        {
            final URI uri = new URI( getConfig().getCollector() );

            HttpPost httpPost = new HttpPost( uri );
            httpPost.setEntity( new ByteArrayEntity( bytes ) );
            httpPost.setHeader( CONTENT_TYPE, APPLICATION_JAVA_OBJECT );
            httpPost.setHeader( X_SIRONA_CLASSNAME, className );

            httpPost.setConfig( requestConfig );
            if ( this.getConfig().isUseAsync() )
            {
                closeableHttpAsyncClient.execute( httpPost, new FutureCallback<HttpResponse>()
                {
                    @Override
                    public void completed( HttpResponse httpResponse )
                    {
                        int status = httpResponse.getStatusLine().getStatusCode();
                        if ( status != 200 )
                        {
                            LOGGER.warning( "Pushed data but response code is: " + status + //
                                                ", reason:" + httpResponse.getStatusLine().getReasonPhrase() );
                        }
                    }

                    @Override
                    public void failed( Exception e )
                    {
                        LOGGER.warning( "Failed to push data: " + e.getMessage() );
                        e.printStackTrace();
                    }

                    @Override
                    public void cancelled()
                    {
                        LOGGER.warning( "Push data cancelled " );
                    }
                } );
            }
            else
            {

                httpclient.execute( httpPost, new ResponseHandler<HttpResponse>()
                {
                    public HttpResponse handleResponse( HttpResponse httpResponse )
                        throws ClientProtocolException, IOException
                    {
                        int status = httpResponse.getStatusLine().getStatusCode();
                        if ( status != 200 )
                        {
                            LOGGER.warning( "Pushed data but response code is: " + status + //
                                                ", reason:" + httpResponse.getStatusLine().getReasonPhrase() );
                        }
                        return httpResponse;
                    }
                } );
            }
        }
        catch ( URISyntaxException e )
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
