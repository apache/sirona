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
package org.apache.sirona.graphite;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Pattern;

public class Graphite implements Closeable {
    private static final char LN = '\n';
    private static final String SPACE = " ";
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final String SPACE_REPLACEMENT = "_";
    private static final String VALUE_FORMAT = "%2.2f";

    private final Charset charset;
    private final SocketFactory factory;
    private final InetAddress address;
    private final int port;

    private BufferedWriter writer = null;
    private Socket socket = null;

    public Graphite(final SocketFactory factory, final InetAddress address, final int port, final Charset charset) throws IOException {
        if (charset != null) {
            this.charset = charset;
        } else {
            this.charset = UTF_8;
        }
        if (factory != null) {
            this.factory = factory;
        } else {
            this.factory = SocketFactory.getDefault();
        }
        this.address = address;
        this.port = port;
    }

    public Graphite(final SocketFactory factory, final String address, final int port, final Charset charset) throws IOException {
        this(factory, InetAddress.getByName(address), port, charset);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void open() throws IOException {
        socket = factory.createSocket(address, port);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
    }

    /**
     * this method is to use with open() and close() once for batch pushes.
     */
    public void push(final String metricPath, final double metricValue, final long metricTimeStamp) throws IOException {
        writer.write(
                WHITESPACE.matcher(noSpace(metricPath)).replaceAll(SPACE_REPLACEMENT) + SPACE
                        + String.format(Locale.US, VALUE_FORMAT, metricValue) + SPACE
                        + metricTimeStamp
                        + LN);
    }

    @Override
    public void close() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (final IOException ioe) {
            // no-op
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (final IOException ioe) {
            // no-op
        }
        writer = null;
        socket = null;
    }

    private static String noSpace(final String s) {
        return s.replace(SPACE, SPACE_REPLACEMENT);
    }

}
