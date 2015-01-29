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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class AgentNode {
    public static final int DEAD_COUNT = 5;
    private final URL url;
    private final AtomicLong missed = new AtomicLong(0);

    public AgentNode(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public URL getUrl() {
        return url;
    }

    public void ok() {
        missed.set(0);
    }

    public void ko() {
        missed.incrementAndGet();
    }

    public boolean isDead() {
        return missed.get() > DEAD_COUNT;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AgentNode agentNode = AgentNode.class.cast(o);
        return url.toExternalForm().equals(agentNode.url.toExternalForm());

    }

    @Override
    public int hashCode() {
        return url.toExternalForm().hashCode();
    }
}
