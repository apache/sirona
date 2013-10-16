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
package org.apache.commons.monitoring.cube;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cube {
    private static final Logger LOGGER = Logger.getLogger(Cube.class.getName());

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String APPLICATION_JSON = "application/json";

    private final CubeBuilder config;
    private final Proxy proxy;

    public Cube(final CubeBuilder cubeBuilder) {
        config = cubeBuilder;
        if (config.getProxyHost() != null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort()));
        } else {
            proxy = Proxy.NO_PROXY;
        }
    }

    public void post(final String payload) {
        try {
            final URL url = new URL(config.getCollector());

            final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection(proxy));
            connection.setRequestMethod(POST);
            connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
            connection.setRequestProperty(CONTENT_LENGTH, Long.toString(payload.length()));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try {
                final OutputStream output = connection.getOutputStream();
                try {
                    output.write(payload.getBytes());
                    output.flush();

                    final int status = connection.getResponseCode();
                    if (status / 100 != 2) {
                        LOGGER.warning("Pushed data but response code is: " + status);
                    }
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Can't post data to collector", e);
        }
    }
}
