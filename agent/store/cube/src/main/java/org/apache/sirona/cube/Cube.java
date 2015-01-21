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

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.ValidationResult;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class Cube {
    private static final Logger LOGGER = Logger.getLogger(Cube.class.getName());

    private static final String COUNTER_TYPE = "counter";
    private static final String GAUGE_TYPE = "gauge";
    private static final String VALIDATION_TYPE = "validation";
    private static final String STATUS_TYPE = "status";
    private static final String PATHTRACKING_TYPE = "pathtracking";

    private static final String NAME = "name";
    private static final String ROLE = "role";
    private static final String UNIT = "unit";
    private static final String CONCURRENCY = "concurrency";
    private static final String MEAN = "mean";
    private static final String VARIANCE = "variance";
    private static final String HITS = "hits";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String SUM = "sum";
    private static final String M_2 = "m2";

    private static final String TRACKING_D = "trackingId";
    private static final String NODE_ID = "nodeId";
    private static final String CLASSNAME = "className";
    private static final String METHOD_NAME = "methodName";
    private static final String START_TIME = "startTime";
    private static final String EXEC_TIME = "executionTime";
    private static final String LEVEL = "level";

    private static final String JSON_BASE = "{" +
        "\"type\": \"%s\"," +
        "\"time\": \"%s\"," +
        "\"data\": %s" +
        "}";

    protected static final String POST = "POST";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_LENGTH = "Content-Length";
    protected static final String GZIP_CONTENT_ENCODING = "gzip";
    protected static final String CONTENT_ENCODING = "Content-Encoding";
    protected static final String APPLICATION_JAVA_OBJECT = "application/x-java-serialized-object";
    protected static final String X_SIRONA_CLASSNAME = "X-Sirona-ClassName";

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

    public String globalPayload(final StringBuilder payload) {
        if (payload.length() > 0) {
            return finalPayload(payload.substring(0, payload.length() - 1));
        }
        return finalPayload(payload.toString());
    }

    public void post(final StringBuilder payload) {
        if (payload.length() > 0) {
            doPost(globalPayload(payload));
        }
    }

    public void doPostBytes( byte[] bytes, String className )
    {
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
            connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JAVA_OBJECT);
            connection.setRequestProperty( X_SIRONA_CLASSNAME, className );
            connection.setRequestProperty(CONTENT_LENGTH, Long.toString(bytes.length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setReadTimeout( config.getPostTimeout() );
            OutputStream output = connection.getOutputStream();
            try {
                // FIXME find a more efficient way to prevent to have all of this in memory
                output.write( bytes );
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

        } catch (final Exception e) {
            if (LOGGER.isLoggable( Level.FINE ) )
            {
                LOGGER.log(Level.FINE, "Can't post data to collector:" + e.getMessage(),e);
            } else
            {
                LOGGER.log( Level.WARNING, "Can't post data to collector: " + e.getMessage() );
            }
        }
    }

    protected void doPost(final String payload) {
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


            boolean useCompression = config.isUseCompression();

            byte[] bytes = useCompression ? gzipCompression( payload.getBytes() ) : payload.getBytes();

            connection.setRequestMethod(POST);
            connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
            if (useCompression) {
                connection.setRequestProperty( CONTENT_ENCODING, GZIP_CONTENT_ENCODING );
            }
            connection.setRequestProperty(CONTENT_LENGTH, Long.toString(bytes.length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setReadTimeout( config.getPostTimeout() );
            OutputStream output = null;


            output = connection.getOutputStream();
            try {
                // FIXME find a more efficient way to prevent to have all of this in memory
                output.write( bytes );
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

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Can't post data to collector", e);
        }
    }

    private static byte[] gzipCompression( byte[] unCompress )
        throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream( buffer );
        out.write( unCompress );
        out.finish();
        ByteArrayInputStream bais = new ByteArrayInputStream( buffer.toByteArray() );
        byte[] res = toByteArray( bais );
        return res;
   }

    public static byte[] toByteArray( InputStream input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer =  new byte[4096];

        int n = 0;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }

        return output.toByteArray();
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

    public StringBuilder counterSnapshot(final Collection<Counter> instances) {
        final long ts = System.currentTimeMillis();
        final StringBuilder events = newEventStream();
        for (final Counter counter : instances) {
            buildEvent(events, COUNTER_TYPE, ts, new MapBuilder()
                .add(NAME, counter.getKey().getName())
                .add(ROLE, counter.getKey().getRole().getName())
                .add(UNIT, counter.getKey().getRole().getUnit().getName())
                // minimum metrics to be able to aggregate counters later
                .add(CONCURRENCY, counter.currentConcurrency().intValue())
                .add(MEAN, counter.getMean())
                .add(VARIANCE, counter.getVariance())
                .add(HITS, counter.getHits())
                .add(MAX, counter.getMax())
                .add(MIN, counter.getMin())
                .add(SUM, counter.getSum())
                .add(M_2, counter.getSecondMoment())
                .map());
        }
        return events;
    }

    public StringBuilder pathTrackingSnapshot( Collection<PathTrackingEntry> pathTrackingEntries ) {
        final StringBuilder events = newEventStream();
        final long ts = System.currentTimeMillis();

        for (PathTrackingEntry pathTrackingEntry : pathTrackingEntries){

            buildEvent( events, PATHTRACKING_TYPE, ts, new MapBuilder()
                    .add( TRACKING_D, pathTrackingEntry.getTrackingId() )
                    .add( NODE_ID, pathTrackingEntry.getNodeId() )
                    .add( CLASSNAME, pathTrackingEntry.getClassName())
                    .add( METHOD_NAME, pathTrackingEntry.getMethodName())
                    .add( START_TIME, pathTrackingEntry.getStartTime())
                    .add( EXEC_TIME, pathTrackingEntry.getExecutionTime())
                    .add( LEVEL, pathTrackingEntry.getLevel())
                    .map()
            );

        }
        return events;
    }

    public StringBuilder pathTrackingSnapshot( PathTrackingEntry pathTrackingEntry ) {
        final StringBuilder event = newEventStream();
        final long ts = System.currentTimeMillis();

        buildEvent( event, PATHTRACKING_TYPE, ts, new MapBuilder()
                        .add( TRACKING_D, pathTrackingEntry.getTrackingId() )
                        .add( NODE_ID, pathTrackingEntry.getNodeId() )
                        .add( CLASSNAME, pathTrackingEntry.getClassName() )
                        .add( METHOD_NAME, pathTrackingEntry.getMethodName() )
                        .add( START_TIME, pathTrackingEntry.getStartTime() )
                        .add( EXEC_TIME, pathTrackingEntry.getExecutionTime() )
                        .add( LEVEL, pathTrackingEntry.getLevel() )
                        .map()
        );


        return event;
    }

    public StringBuilder gaugeSnapshot(final StringBuilder base, final long time, final Role role, final double value) {
        return buildEvent(base, GAUGE_TYPE, time,
            new MapBuilder()
                .add("value", value)
                .add("role", role.getName())
                .add("unit", role.getUnit().getName())
                .map());
    }

    public StringBuilder statusSnapshot(final long ts, final NodeStatus nodeStatus) {
        final StringBuilder events = newEventStream();
        for (final ValidationResult result : nodeStatus.getResults()) {
            buildEvent(events, VALIDATION_TYPE, ts, new MapBuilder()
                .add("message", result.getMessage())
                .add("status", result.getStatus().name())
                .add("name", result.getName())
                .map());
        }
        if (nodeStatus.getDate() != null) {
            buildEvent(events, STATUS_TYPE, ts, new MapBuilder()
                .add("date", nodeStatus.getDate().getTime())
                .map());
        }
        return events;
    }

    protected CubeBuilder getConfig()
    {
        return config;
    }
}
