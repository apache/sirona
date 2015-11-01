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

import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.util.Localhosts;

import java.util.concurrent.atomic.AtomicReference;

@AutoSet
public class WebSocketClientBuilder {
    private String uri;
    private String marker;
    private int retries;
    private String authorization;
    private AtomicReference<WebSocketClient> client = new AtomicReference<WebSocketClient>();

    public void setMarker(final String marker) {
        this.marker = marker;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public void setRetries(final int retries) {
        this.retries = retries;
    }

    public void setAuthorization(final String authorization) {
        this.authorization = authorization;
    }

    public WebSocketClient buildOrGet() {
        WebSocketClient webSocketClient = client.get();
        if (webSocketClient == null) {
            webSocketClient = new WebSocketClient(retries, uri.endsWith("/") ? uri : (uri + '/'), authorization, marker == null ? Localhosts.get(): marker);
            if (!client.compareAndSet(null, webSocketClient)) {
                webSocketClient.close();
                webSocketClient = client.get();
            }
        }
        return webSocketClient;
    }
}
