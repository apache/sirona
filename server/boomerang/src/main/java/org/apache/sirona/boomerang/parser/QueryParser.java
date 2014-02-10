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

public class QueryParser {
    public static BoomerangData parse(final Map<String, String> params) {
        final BoomerangData data = new BoomerangData(params);

        final String url = params.get("u");
        if (url != null) {
            data.setUrl(url);
        }
        // not yet used but depending on next releases it could
        final String version = params.get("v");
        if (version != null) {
            data.setVersion(version);
        }

        final String tDone = params.get("t_done");
        if (tDone != null && !tDone.isEmpty()) {
            data.setTDone(parseLong(tDone));
        }

        final String bw = params.get("bw");
        if (bw != null && !bw.isEmpty()) {
            data.setBandwidth(parseLong(bw));
        }

        final String lat = params.get("lat");
        if (lat != null && !lat.isEmpty()) {
            data.setLatency(parseLong(lat));
        }

        return data;
    }

    private static Long parseLong(final String value) {
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException nbe) {
            return null;
        }
    }

    private QueryParser() {
        // no-op
    }
}
