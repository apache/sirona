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

package org.apache.commons.monitoring.web.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Select the category to use for a requested URI by searching a matching pattern
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CategorizedMonitoringFilter extends MonitoringFilter {
    /**
     * Ordered Map of patterns that defines categories. First pattern matching the requested URI define the category
     */
    private final Map<Pattern, String> categories = new LinkedHashMap<Pattern, String>();

    @Override
    protected String getCategory(final String uri) {
        for (final Map.Entry<Pattern, String> entry : categories.entrySet()) {
            final Pattern pattern = entry.getKey();
            if (pattern.matcher(uri).matches()) {
                return entry.getValue();
            }
        }
        return super.getCategory(uri);
    }

    /**
     * read "categories" configuration, format is:
     *
     *     &lt;pattern1&gt; = &lt;category1&gt;
     *     &lt;pattern2&gt; = &lt;category2&gt;
     *
     * @param config the filter vconfig
     * @throws ServletException
     */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        super.init(config);

        final String rawCat = config.getInitParameter("categories");
        final StringReader reader = new StringReader(rawCat);
        final Properties props = new Properties();
        try {
            props.load(reader);
        } catch (final IOException e) {
            // no-op
        }

        for (final String key : props.stringPropertyNames()) {
            categories.put(Pattern.compile(key), props.getProperty(key));
        }
    }
}
