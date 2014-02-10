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
package org.apache.sirona.boomerang.parser;

import java.util.Map;

public class BoomerangData {
    private final Map<String, String> rawValues;
    private String url = null;
    private String version = null;
    private Long tDone = null;
    private Long bandwidth = null;
    private Long latency = null;

    public BoomerangData(final Map<String, String> rawValues) {
        this.rawValues = rawValues;
    }

    public boolean validate() {
        return url != null && !url.isEmpty();
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public Long getTDone() {
        return tDone;
    }

    public void setTDone(final Long tDone) {
        this.tDone = tDone;
    }

    public void setBandwidth(final Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public void setLatency(final Long latency) {
        this.latency = latency;
    }

    public Long getLatency() {
        return latency;
    }
}
