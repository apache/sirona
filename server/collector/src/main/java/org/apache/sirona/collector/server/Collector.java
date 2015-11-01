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
package org.apache.sirona.collector.server;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.johnzon.mapper.Converter;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.collector.server.api.SSLSocketFactoryProvider;
import org.apache.sirona.collector.server.api.SecurityProvider;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.util.DaemonThreadFactory;
import org.apache.sirona.util.SerializeUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

// should work with cube clients, see cube module for details
// Note: for this simple need we don't need JAXRS
public class Collector extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Collector.class.getName());

    private static final String OK = "{}";
    private static final String GAUGE = "gauge";
    private static final String COUNTER = "counter";
    private static final String VALIDATION = "validation";
    private static final String STATUS = "status";
    private static final String REGISTRATION = "registration";
    private static final String PATH_TRACKING = "pathtracking";

    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JAVA_OBJECT = "application/x-java-serialized-object";
    private static final String X_SIRONA_CLASSNAME = "X-Sirona-ClassName";


    private static final String GET = "GET";

    private final Map<String, Role> roles = new ConcurrentHashMap<String, Role>();

    private CollectorCounterStore counterDataStore = null;
    private CollectorGaugeDataStore gaugeDataStore = null;
    private CollectorNodeStatusDataStore statusDataStore;
    private CollectorPathTrackingDataStore pathTrackingDataStore;
    private Mapper mapper;

    private final Collection<AgentNode> agents = new CopyOnWriteArraySet<AgentNode>();
    private volatile BatchFuture collectionFuture = null;
    private long collectionPeriod;
    private SecurityProvider securityProvider;
    private SSLSocketFactoryProvider sslSocketFactoryProvider;

    private boolean disableDisruptor;

    private RingBuffer<PathTrackingEntry> ringBuffer;

    private Disruptor<PathTrackingEntry> disruptor;



    @Override
    public void init(final ServletConfig sc) throws ServletException {
        super.init(sc);

        // force init to ensure we have stores
        IoCs.findOrCreateInstance(Repository.class);

        {
            final CollectorGaugeDataStore gds = IoCs.findOrCreateInstance(CollectorGaugeDataStore.class);
            if (gds == null) {
                throw new IllegalStateException("Collector only works with " + CollectorGaugeDataStore.class.getName());
            }
            this.gaugeDataStore = CollectorGaugeDataStore.class.cast(gds);
        }

        {
            final CollectorCounterStore cds = IoCs.findOrCreateInstance(CollectorCounterStore.class);
            if (cds == null) {
                throw new IllegalStateException("Collector only works with " + CollectorCounterStore.class.getName());
            }
            this.counterDataStore = CollectorCounterStore.class.cast(cds);
        }

        {
            final NodeStatusDataStore nds = IoCs.findOrCreateInstance(CollectorNodeStatusDataStore.class);
            if (!CollectorNodeStatusDataStore.class.isInstance(nds)) {
                throw new IllegalStateException("Collector only works with " + CollectorNodeStatusDataStore.class.getName());
            }
            this.statusDataStore = CollectorNodeStatusDataStore.class.cast(nds);
        }

        {
            this.pathTrackingDataStore = IoCs.findOrCreateInstance( CollectorPathTrackingDataStore.class );
            // TODO validation
        }

        final MapperBuilder mapperBuilder = new MapperBuilder();
        final Queue<DateFormat> dateFormatCache = new ConcurrentLinkedQueue<DateFormat>();
        for (int i = 0; i < 16; i++) {
            dateFormatCache.add(newSimpleDateFormat(null));
        }
        mapperBuilder.addConverter(Date.class, new Converter<Date>() {
            @Override
            public String toString(final Date instance) {
                final DateFormat dateFormat = newSimpleDateFormat(dateFormatCache);
                try {
                    return dateFormat.format(instance);
                } finally {
                    dateFormatCache.add(dateFormat);
                }
            }

            @Override
            public Date fromString(final String text) {
                final DateFormat dateFormat = newSimpleDateFormat(dateFormatCache);
                try {
                    return dateFormat.parse(text);
                } catch (final ParseException e) {
                    throw new IllegalArgumentException(e);
                } finally {
                    dateFormatCache.add(dateFormat);
                }
            }
        });
        mapper = mapperBuilder.build();

        { // pulling
            {
                final String periodKey = Configuration.CONFIG_PROPERTY_PREFIX + "collector.collection.period";
                final String collectionPeriodStr = sc.getInitParameter(periodKey);
                if (collectionPeriodStr != null) {
                    collectionPeriod = Integer.parseInt(collectionPeriodStr);
                } else {
                    collectionPeriod = Configuration.getInteger(periodKey, 60000);
                }
            }

            {
                final String agentUrlsKey = Configuration.CONFIG_PROPERTY_PREFIX + "collector.collection.agent-urls";
                for (final String agents : new String[]{
                    Configuration.getProperty(agentUrlsKey, null),
                    sc.getInitParameter(agentUrlsKey)
                }) {
                    if (agents != null) {
                        for (final String url : agents.split(",")) {
                            try {
                                registerNode(url.trim());
                            } catch (final MalformedURLException e) {
                                throw new SironaException(e);
                            }
                        }
                    }
                }
            }

            try {
                securityProvider = IoCs.findOrCreateInstance(SecurityProvider.class);
            } catch (final Exception e) {
                securityProvider = null;
            }

            try {
                sslSocketFactoryProvider = IoCs.findOrCreateInstance(SSLSocketFactoryProvider.class);
            } catch (final Exception e) {
                sslSocketFactoryProvider = null;
            }
        }
        { // disruptor or not
            String key = Configuration.CONFIG_PROPERTY_PREFIX + "collector.pathtracking.disabledisruptor";
            this.disableDisruptor = Boolean.parseBoolean( Configuration.getProperty( key, "false" ) );
            if ( !this.disableDisruptor )
            {
                ExecutorService exec = Executors.newCachedThreadPool(); // FIXME: proper config

                key = Configuration.CONFIG_PROPERTY_PREFIX + "collector.pathtracking.disruptor.ringBufferSize";

                int ringBufferSize = Configuration.getInteger( key, 4096 );

                key = Configuration.CONFIG_PROPERTY_PREFIX + "collector.pathtracking.disruptor.numberOfConsumers";

                int numberOfConsumers = Configuration.getInteger( key, 4 );

                // FIXME make configurable: WaitStrategy

                disruptor = new Disruptor<PathTrackingEntry>( new EventFactory<PathTrackingEntry>()
                {
                    @Override
                    public PathTrackingEntry newInstance()
                    {
                        return new PathTrackingEntry();
                    }
                }, ringBufferSize, exec, ProducerType.SINGLE, new BusySpinWaitStrategy()
                );

                for ( int i = 0; i < numberOfConsumers; i++ )
                {
                    disruptor.handleEventsWith( new PathTrackingEntryEventHandler( i, numberOfConsumers, this.pathTrackingDataStore ) );
                }
                ringBuffer = disruptor.start();
            }
        }
    }

    private DateFormat newSimpleDateFormat(final Queue<DateFormat> dateFormatCache) {
        if (dateFormatCache == null) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat;
        }
        final DateFormat df = dateFormatCache.poll();
        if (df == null) {
            return newSimpleDateFormat(null);
        }
        return df;
    }

    private static class PathTrackingEntryEventHandler
        implements EventHandler<PathTrackingEntry>
    {

        private final long ordinal;

        private final long numberOfConsumers;

        private final CollectorPathTrackingDataStore pathTrackingDataStore;

        public PathTrackingEntryEventHandler( final long ordinal, final long numberOfConsumers, CollectorPathTrackingDataStore pathTrackingDataStore )
        {
            this.ordinal = ordinal;
            this.numberOfConsumers = numberOfConsumers;
            this.pathTrackingDataStore = pathTrackingDataStore;
        }

        public void onEvent( final PathTrackingEntry entry, final long sequence, final boolean endOfBatch )
            throws Exception
        {
            if ( ( sequence % numberOfConsumers ) == ordinal )
            {
                pathTrackingDataStore.store( entry );
            }
        }

    }


    @Override
    public void destroy() {
        if (collectionFuture != null) {
            collectionFuture.done();
        }
        if (this.disruptor != null) {
            // FIXME make timeout configurable?
            try
            {
                disruptor.shutdown( 1000, TimeUnit.MILLISECONDS );
            } catch ( TimeoutException e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {

        final ServletInputStream inputStream = req.getInputStream();
        try {
            if (APPLICATION_JAVA_OBJECT.equals( req.getHeader( CONTENT_TYPE ) )) {
                if (PathTrackingEntry.class.getName().equals( req.getHeader( X_SIRONA_CLASSNAME ) )) {
                    int length = req.getContentLength();
                    updatePathTracking( readBytes( req.getInputStream(), length ) );
                }
            }
            else
            {
                if ( "gzip".equals( req.getHeader( CONTENT_ENCODING ) ) )
                {
                    slurpEvents( new GZIPInputStream( inputStream ) );
                }
                else
                {
                    slurpEvents( inputStream );
                }
            }
        } catch (final SironaException me) {
            resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + me.getCause().getMessage().replace('\"', ' ') + "\"}");
            return;
        }

        resp.setStatus(HttpURLConnection.HTTP_OK);
        resp.getWriter().write(OK);
    }

    private byte[] readBytes(ServletInputStream servletInputStream, int length)
        throws IOException
    {
        byte[] bytes = new byte[length];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(length);

        int nRead;

        while ((nRead = servletInputStream.read(bytes, 0, bytes.length)) != -1) {
            buffer.write(bytes, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();

    }

    private void slurpEvents(final InputStream inputStream) throws IOException {
        final Event[] events = mapper.readArray(inputStream, Event.class);
        if (events != null && events.length > 0) {
            try {
                final Collection<Event> validations = new LinkedList<Event>();
                long date = -1;
                for (final Event event : events) {
                    final String type = event.getType();
                    if (VALIDATION.equals(type)) {
                        validations.add(event);
                    } else if (STATUS.equals(type)) {
                        date = Number.class.cast(event.getData().get("date")).longValue();
                    } else if (COUNTER.equals(type)) {
                        updateCounter(event);
                    } else if (GAUGE.equals(type)) {
                        updateGauge(event);
                    } else if (REGISTRATION.equals(type)) {
                        registerNode( event );
                    } else if (PATH_TRACKING.equals(type)) {
                        updatePathTracking(event);
                    } else {
                        LOGGER.info("Unexpected type '" + type + "', skipping");
                    }
                }

                if (validations.size() > 0) {
                    final Collection<ValidationResult> results = new ArrayList<ValidationResult>(validations.size());
                    for (final Event event : validations) {
                        final Map<String, Object> data = event.getData();
                        results.add(new ValidationResult(
                            (String) data.get("name"),
                            Status.valueOf((String) data.get("status")),
                            (String) data.get("message")));
                    }

                    final Date statusDate;
                    if (date == -1) {
                        statusDate = new Date();
                    } else {
                        statusDate = new Date(date);
                    }
                    final NodeStatus status = new NodeStatus(results.toArray(new ValidationResult[results.size()]), statusDate);
                    statusDataStore.store((String) events[0].getData().get("marker"), status);
                }
            } catch (final Exception e) {
                throw new SironaException(e);
            }
        }
    }

    private void registerNode(final Event event) throws MalformedURLException {
        registerNode(String.class.cast(event.getData().get("url")));
    }

    private void registerNode(final String url) throws MalformedURLException {
        if (url == null) {
            return;
        }

        final AgentNode node = new AgentNode(url);
        if (agents.add(node)) {
            if (collectionFuture == null) {
                synchronized (this) {
                    if (collectionFuture == null) {
                        final ScheduledExecutorService ses =
                            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("collector-pull-schedule"));
                        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new CollectTask(), //
                                                                                  collectionPeriod, //
                                                                                  collectionPeriod, //
                                                                                  TimeUnit.MILLISECONDS);
                        collectionFuture = new BatchFuture(ses, future);
                    }
                }
            }
        }
    }

    private void updateGauge(final Event event) {
        final Map<String, Object> data = event.getData();

        final long time = event.getTime().getTime();
        final double value = Number.class.cast(data.get("value")).doubleValue();

        gaugeDataStore.addToGauge(role(data), time, value, String.class.cast(data.get("marker")));
    }

    private void updatePathTracking(final Event event) {
        final Map<String, Object> data = event.getData();

        final PathTrackingEntry pathTrackingEntry =
            new PathTrackingEntry(  String.class.cast(data.get("trackingId")),//
                                    String.class.cast(data.get("nodeId")), //
                                    String.class.cast(data.get("className")), //
                                    String.class.cast(data.get("methodName")), //
                                    Number.class.cast(data.get("startTime")).longValue(), //
                                    Number.class.cast(data.get("executionTime")).longValue(), //
                                    Number.class.cast(data.get("level") ).intValue() );

        if (this.disableDisruptor) {
            pathTrackingDataStore.store( pathTrackingEntry );
        } else {
            ringBuffer.publishEvent( new EventTranslator<PathTrackingEntry>()
            {
                @Override
                public void translateTo( PathTrackingEntry event, long sequence )
                {
                    event.setClassName( pathTrackingEntry.getClassName() );
                    event.setExecutionTime( pathTrackingEntry.getExecutionTime() );
                    event.setLevel( pathTrackingEntry.getLevel() );
                    event.setMethodName( pathTrackingEntry.getMethodName() );
                    event.setNodeId( pathTrackingEntry.getNodeId() );
                    event.setStartTime( pathTrackingEntry.getStartTime() );
                    event.setTrackingId( pathTrackingEntry.getTrackingId() );
                }
            } );
        }
    }


    private void updatePathTracking(final byte[] bytes) {

        final PathTrackingEntry pathTrackingEntry = SerializeUtils.deserialize( bytes, PathTrackingEntry.class );

        if (this.disableDisruptor)
        {
            pathTrackingDataStore.store( pathTrackingEntry );
        } else {
            ringBuffer.publishEvent( new EventTranslator<PathTrackingEntry>()
            {
                @Override
                public void translateTo( PathTrackingEntry event, long sequence )
                {
                    event.setClassName( pathTrackingEntry.getClassName() );
                    event.setExecutionTime( pathTrackingEntry.getExecutionTime() );
                    event.setLevel( pathTrackingEntry.getLevel() );
                    event.setMethodName( pathTrackingEntry.getMethodName() );
                    event.setNodeId( pathTrackingEntry.getNodeId() );
                    event.setStartTime( pathTrackingEntry.getStartTime() );
                    event.setTrackingId( pathTrackingEntry.getTrackingId() );
                }
            } );
        }
    }



    private void updateCounter(final Event event) {
        final Map<String, Object> data = event.getData();

        counterDataStore.update( new Counter.Key( role( data ), String.class.cast( data.get( "name" ) ) ),
                                 String.class.cast( data.get( "marker" ) ), new M2AwareStatisticalSummary( data ),
                                 Number.class.cast( data.get( "concurrency" ) ).intValue() );
    }

    private Role role(final Map<String, Object> data) {
        final String name = String.class.cast( data.get( "role" ) );
        final Role existing = roles.get(name);
        if (existing != null) {
            return existing;
        }

        final Role created = new Role(name, Unit.get(String.class.cast(data.get("unit"))));
        roles.put(name, created);
        return created;
    }

    private class CollectTask implements Runnable {
        @Override
        public void run() {
            final Iterator<AgentNode> nodes = agents.iterator();
            while (nodes.hasNext()) {
                final AgentNode agent = nodes.next();
                try {
                    final URL url = agent.getUrl();
                    final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());

                    if (sslSocketFactoryProvider != null) {
                        final SSLSocketFactory sf = sslSocketFactoryProvider.sslSocketFactory(url.toExternalForm());
                        if (sf != null && "https".equals(agent.getUrl().getProtocol())) {
                            HttpsURLConnection.class.cast(connection).setSSLSocketFactory(sf);
                        }
                    }

                    if (securityProvider != null) {
                        final String auth = securityProvider.basicHeader(url.toExternalForm());
                        if (auth != null) {
                            connection.setRequestProperty("Authorization", auth);
                        }
                    }

                    connection.setRequestMethod(GET);

                    InputStream inputStream = null;
                    try {
                        inputStream = connection.getInputStream();
                        slurpEvents(inputStream);
                    } finally {
                        connection.disconnect();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (final IOException ioe) {
                                // no-op
                            }
                        }
                    }

                    final int status = connection.getResponseCode();
                    if (status / 100 == 2) {
                        agent.ok();
                    } else {
                        agent.ko();
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    agent.ko();
                }

                if (agent.isDead()) {
                    nodes.remove();
                }
            }
        }
    }
}
