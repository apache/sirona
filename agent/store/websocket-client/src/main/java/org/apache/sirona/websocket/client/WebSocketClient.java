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
package org.apache.sirona.websocket.client;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;
import org.apache.sirona.websocket.client.domain.WSCounter;
import org.apache.sirona.websocket.client.domain.WSGauge;
import org.apache.sirona.websocket.client.domain.WSValidation;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class WebSocketClient implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());

    private final URI counterUri;
    private final URI gaugeUri;
    private final URI validationUri;
    private final int maxSendTries;
    private final String authorization;
    private final WebSocketContainer container;
    private final AtomicReference<Session> counterSession = new AtomicReference<Session>();
    private final AtomicReference<Session> gaugeSession = new AtomicReference<Session>();
    private final AtomicReference<Session> validationSession = new AtomicReference<Session>();
    private final Mapper mapper = new MapperBuilder().build();
    private final String marker;

    public WebSocketClient(final int retries, final String uri, final String authorization, final String marker) {
        this.counterUri = URI.create(uri + "/wsirona/counter");
        this.gaugeUri = URI.create(uri + "/wsirona/gauge");
        this.validationUri = URI.create(uri + "/wsirona/validation");
        this.maxSendTries = 1 + Math.max(0, retries);
        this.authorization = authorization;
        this.marker = marker;
        this.container = ContainerProvider.getWebSocketContainer();
    }

    public void push(final Counter counter) {
        send(counterSession, counterUri, mapper.writeObjectAsString(new WSCounter(counter, marker)));
    }

    public void push(final Role key, final BatchGaugeDataStoreAdapter.Measure value) {
        send(gaugeSession, gaugeUri, mapper.writeObjectAsString(new WSGauge(key, value, marker)));
    }

    public void push(final NodeStatus nodeStatus) {
        for (final ValidationResult validation : nodeStatus.getResults()) {
            send(validationSession, validationUri, mapper.writeObjectAsString(new WSValidation(validation, nodeStatus.getDate(), marker)));
        }
    }

    private synchronized void send(final AtomicReference<Session> sessionRef, final URI uri, final String data) {
        for (int i = 0; i < maxSendTries; i++) {
            try {
                Session session = sessionRef.get();
                if (needsSession(session)) {
                    synchronized (this) {
                        session = sessionRef.get();
                        if (needsSession(session)) {
                            session = connection(uri);
                            if (!sessionRef.compareAndSet(null, session)) {
                                session.close();
                                session = sessionRef.get();
                            }
                        }
                    }
                }

                // sync to avoid issue on server and keep it simple for now
                session.getBasicRemote().sendText(data);
                return; // done :)
            } catch (final Exception ex) { // on exception recreate the connection and retry
                LOGGER.log(Level.SEVERE, "Can't send data, will retry if possible", ex);
                synchronized (this) {
                    try {
                        final Session session = sessionRef.get();
                        if (session != null) {
                            sessionRef.set(null);
                            session.close(new CloseReason(CloseReason.CloseCodes.TRY_AGAIN_LATER, ex.getMessage()));
                        }
                    } catch (final IOException e) {
                        // no-op
                    } catch (final IllegalStateException e) {
                        // no-op
                    }
                }

                try { // retry but wait a little bit
                    sleep(100);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
        throw new IllegalStateException("Can't send '" + data + "' in " + maxSendTries + " tries");
    }

    private boolean needsSession(final Session session) {
        return session == null || !session.isOpen();
    }

    private Session connection(final URI uri) {
        final ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(final Map<String, List<String>> headers) {
                    if (WebSocketClient.this.authorization != null) {
                        headers.put("Authorization", singletonList(WebSocketClient.this.authorization));
                    }
                    super.beforeRequest(headers);
                }
            })
            .build();
        for (int i = 0; i < maxSendTries; i++) {
            try {
                return container.connectToServer(new Endpoint() {
                    @Override
                    public void onOpen(final Session session, final EndpointConfig endpointConfig) {
                        // no-op
                    }
                }, config, uri);
            } catch (final DeploymentException e) {
                throw new IllegalArgumentException(e);
            } catch (final IOException e) {
                // no-op: retry
            }
        }
        throw new IllegalStateException("Cannot connect to " + uri);
    }

    @Override
    public synchronized void close() {
        for (final AtomicReference<Session> ref : asList(counterSession, gaugeSession, validationSession)) {
            final Session s = ref.get();
            if (s != null) {
                try {
                    s.close();
                } catch (final IOException e) {
                    // no-op
                }
                ref.set(null);
            }
        }
    }
}
