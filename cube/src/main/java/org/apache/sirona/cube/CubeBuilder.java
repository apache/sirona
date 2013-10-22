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

import org.apache.sirona.configuration.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration.AutoSet
public class CubeBuilder {
    private static final String DEFAULT_MARKER = "sirona";

    private String proxyHost;
    private int proxyPort;
    private String collector;
    private String marker;

    public Cube build() {
        if (marker == null) {
            try {
                marker = InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException e) {
                marker = DEFAULT_MARKER;
            }
        }

        return new Cube(this);
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getCollector() {
        return collector;
    }

    public String getMarker() {
        return marker;
    }

    @Override
    public String toString() {
        return "CubeBuilder{" + collector + '}';
    }
}
