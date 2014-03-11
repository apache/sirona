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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Olivier Lamy
 */
public class HttpClientCube
    extends Cube
{

    private static final Logger LOGGER = Logger.getLogger(HttpClientCube.class.getName());

    private HttpClient httpclient;

    public HttpClientCube( CubeBuilder cubeBuilder )
    {
        super( cubeBuilder );
        try
        {
            HttpClientBuilder builder = HttpClientBuilder.create();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

            builder.setConnectionManager( connectionManager );

            httpclient = builder.build();


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
            final URI uri = new URI(getConfig().getCollector());

            HttpPost httpPost = new HttpPost( uri );
            httpPost.setEntity( new ByteArrayEntity( bytes ) );
            httpPost.setHeader( CONTENT_TYPE, APPLICATION_JAVA_OBJECT );
            httpPost.setHeader( X_SIRONA_CLASSNAME, className );

            httpclient.execute( httpPost );
        }
        catch ( URISyntaxException e )
        {
            if (LOGGER.isLoggable( Level.FINE ) )
            {
                LOGGER.log(Level.FINE, "Can't post data to collector:" + e.getMessage(),e);
            } else
            {
                LOGGER.log( Level.WARNING, "Can't post data to collector: " + e.getMessage() );
            }
        } catch ( IOException e)
        {
            if (LOGGER.isLoggable( Level.FINE ) )
            {
                LOGGER.log(Level.FINE, "Can't post data to collector:" + e.getMessage(),e);
            } else
            {
                LOGGER.log( Level.WARNING, "Can't post data to collector: " + e.getMessage() );
            }
        }
    }
}
