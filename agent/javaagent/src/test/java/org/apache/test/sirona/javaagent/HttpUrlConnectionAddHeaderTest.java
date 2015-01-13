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
package org.apache.test.sirona.javaagent;

import com.sun.net.httpserver.HttpServer;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;

@RunWith(JavaAgentRunner.class) // proove we can enhance JVM classes
public class HttpUrlConnectionAddHeaderTest {
    private static HttpServer server;

    @BeforeClass
    public static void startHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(nextPort()), 0);
        server.start();
    }

    private static int nextPort() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(0);
        try {
            return serverSocket.getLocalPort();
        } finally {
            serverSocket.close();
        }
    }

    @AfterClass
    public static void stopServer() {
        server.stop(0);
    }

    @Test
    public void addHeader() throws IOException {
        final URL url = new URL("http://localhost:" + server.getAddress().getPort());
        final URLConnection connection = url.openConnection();
        connection.setConnectTimeout(200);
        connection.setReadTimeout(100);
        try {
            connection.connect();
        } catch (final Exception e) {
            // we don't care if the connection fails
        } finally {
            try {
                HttpURLConnection.class.cast(connection).disconnect();
            } catch (final Exception e) {
                // no-op
            }
        }

        assertEquals("sirona", connection.getRequestProperty("origin-test"));
    }

    public static class HttpUrlConnectionHeaderAdder implements InvocationListener {
        @Override
        public void before(final AgentContext context) {
            HttpURLConnection.class.cast(context.getReference()).setRequestProperty("origin-test", "sirona");
        }

        @Override
        public void after(final AgentContext context, final Object result, final Throwable error) {
            // no-op
        }

        @Override
        public boolean accept(final String key, final byte[] rawClassBuffer) {
            return key.equals("sun.net.www.protocol.http.HttpURLConnection.connect()");
        }
    }
}
