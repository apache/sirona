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
package org.apache.commons.monitoring.reporting.web.handler.api;

import java.util.Collections;
import java.util.Map;

public class Template {
    private final String template;
    private final Map<String, ?> userParams;
    private final boolean html;

    public Template(final String template) {
        this(template, Collections.<String, Object>emptyMap());
    }

    public Template(final String template, final Map<String, ?> userParams) {
        this(template, userParams, true);
    }

    /**
     * @param template template path, if isHtml is true it is relative to /templates otherwise it is absolute.
     * @param userParams variables used by the template
     * @param isHtml should the template be rendered with the site them or not (= is it a page fragment).
     */
    public Template(final String template, final Map<String, ?> userParams, final boolean isHtml) {
        this.template = template;
        this.userParams = userParams;
        this.html = isHtml;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, ?> getUserParams() {
        return userParams;
    }

    public boolean isHtml() {
        return html;
    }
}
