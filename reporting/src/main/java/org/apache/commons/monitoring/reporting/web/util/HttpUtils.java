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

package org.apache.commons.monitoring.reporting.web.util;

public final class HttpUtils {

    public static String parseAccept(String header) {
        return getPrefered(header);
    }

    private static String getPrefered(String header) {
        if (header == null) {
            return null;
        }
        final String[] languages = header.split(",");
        String prefered = null;
        double preference = 0.0D;
        for (String language : languages) {
            int idx = language.indexOf(';');
            if (idx > 0) {
                String paramString = language.substring(idx + 1);
                double d = getQuality(paramString);
                if (d > preference) {
                    preference = d;
                    prefered = language.substring(0, idx);
                }
            } else {
                return language;
            }
        }
        return prefered;
    }

    private static double getQuality(String paramString) {
        final String[] params = paramString.split(";");
        for (final String param : params) {
            if (param.startsWith("q=")) {
                return Double.parseDouble(param.substring(2));
            }
        }
        return 1;
    }

    private HttpUtils() {
        // no-op
    }
}
