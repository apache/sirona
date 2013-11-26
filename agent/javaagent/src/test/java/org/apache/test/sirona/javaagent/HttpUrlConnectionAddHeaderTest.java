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

import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;

@RunWith(JavaAgentRunner.class)
public class HttpUrlConnectionAddHeaderTest {
    @Test
    public void addHeader() throws IOException {
        final URLConnection connection = ConnectionFactory.createConnection();
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

    // we can't do it on JVm classes directly ATM (sun HttpURLConnection) so using another abstraction
    public static class ConnectionFactory {
        private ConnectionFactory() {
            // no-op
        }

        private static URLConnection createConnection() throws IOException {
            final URL url = new URL("http://doesntexist:12354");
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(200);
            connection.setReadTimeout(100);
            return connection;
        }
    }

    public static class HttpUrlConnectionHeaderAdder implements InvocationListener {
        @Override
        public void before(final AgentContext context) {
            // no-op
        }

        @Override
        public void after(final AgentContext context, final Object result, final Throwable error) {
            HttpURLConnection.class.cast(result).setRequestProperty("origin-test", "sirona");
        }

        @Override
        public boolean accept(final Counter.Key key, final Object instance) { // static => instance == null
            return key.getName().equals("org.apache.test.sirona.javaagent.HttpUrlConnectionAddHeaderTest$ConnectionFactory.createConnection");
        }
    }
}
