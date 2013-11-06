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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cube {
    private static final Logger LOGGER = Logger.getLogger(Cube.class.getName());

    private static final String JSON_BASE = "{" +
        "\"type\": \"%s\"," +
        "\"time\": \"%s\"," +
        "\"data\": %s" +
        "}";

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String APPLICATION_JSON = "application/json";

    private static final String JS_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UTC = "UTC";

    private final CubeBuilder config;
    private final Proxy proxy;

    private final BlockingQueue<DateFormat> isoDateFormatters;

    public Cube(final CubeBuilder cubeBuilder) {
        config = cubeBuilder;
        if (config.getProxyHost() != null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort()));
        } else {
            proxy = Proxy.NO_PROXY;
        }

        final int maxConcurrency = 2 * Runtime.getRuntime().availableProcessors();
        isoDateFormatters = new ArrayBlockingQueue<DateFormat>(maxConcurrency);
        for (int i = 0; i < maxConcurrency; i++) {
            isoDateFormatters.add(newIsoDateFormatter());
        }
    }

    public StringBuilder newEventStream() {
        return new StringBuilder();
    }

    public void post(final StringBuilder payload) {
        if (payload.length() > 0) {
            doPost(finalPayload(payload.substring(0, payload.length() - 1)));
        }
    }

    private void doPost(final String payload) {
        try {
            final URL url = new URL(config.getCollector());

            final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection(proxy));

            final SSLSocketFactory socketFactory = config.getSocketFactory();
            if (socketFactory != null && "https".equals(url.getProtocol())) {
                HttpsURLConnection.class.cast(connection).setSSLSocketFactory(socketFactory);
            }

            final String auth = config.getBasicHeader();
            if (auth != null) {
                connection.setRequestProperty("Authorization", auth);
            }

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

    public static String finalPayload(final String events) {
        return '[' + events + ']';
    }

    public StringBuilder buildEvent(final StringBuilder builder, final String type, final long time, final Map<String, Object> data) {
        data.put("marker", config.getMarker());
        return builder.append(String.format(JSON_BASE, type, isoDate(time), buildData(data))).append(',');
    }

    private String isoDate(final long time) {
        final Date date = new Date(time);

        DateFormat formatter = null;
        try {
            formatter = isoDateFormatters.take();
            return formatter.format(date);
        } catch (final InterruptedException e) {
            return newIsoDateFormatter().format(date);
        } finally {
            if (formatter != null) {
                isoDateFormatters.add(formatter);
            }
        }
    }

    private static String buildData(final Map<String, Object> data) {
        final StringBuilder builder = new StringBuilder().append("{");
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            builder.append('\"').append(entry.getKey()).append('\"').append(':');

            final Object value = entry.getValue();
            if (String.class.isInstance(value)) {
                builder.append('\"').append(value).append('\"');
            } else {
                builder.append(value);
            }

            builder.append(',');
        }
        builder.setLength(builder.length() - 1);
        return builder.append("}").toString();
    }

    private static DateFormat newIsoDateFormatter() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(JS_ISO_FORMAT, Locale.ENGLISH);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(UTC));
        return simpleDateFormat;
    }

}
