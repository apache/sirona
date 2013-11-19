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
package org.apache.sirona.reporting.web.plugin.thread;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;

import java.util.Map;
import java.util.TreeMap;

public class ThreadEndpoints {
    @Regex
    public Template home() {
        return new Template("threads/threads.vm", new MapBuilder<String, Object>()
                                .set("threads", listThreads())
                                .build());
    }

    @Regex("/([^/]*)")
    public Template dump(final String name) {
        final Thread thread = findThread(new String(Base64.decodeBase64(name)));
        if (thread == null) {
            return new Template("templates/threads/thread.vm", new MapBuilder<String, Object>().set("state", "Not found").build(), false);
        }

        return new Template(
            "templates/threads/thread.vm",
                new MapBuilder<String, Object>().set("state", thread.getState().name()).set("dump", StringEscapeUtils.escapeHtml4(dump(thread))).build(),
                false);
    }

    private static Thread findThread(final String name) {
        int count = Thread.activeCount();
        final Thread[] threads = new Thread[count];
        count = Thread.enumerate(threads);

        for (int i = 0; i < count; i++) {
            if (threads[i].getName().equals(name)) {
                return threads[i];
            }
        }

        return null;
    }

    private static String dump(final Thread thread) {
        final StackTraceElement[] stack = thread.getStackTrace();
        final StringBuilder builder = new StringBuilder();
        for (final StackTraceElement element : stack) {
            builder.append(element.toString()).append("\n"); // toString method is fine
        }
        return builder.toString();
    }

    private static Map<String, String> listThreads() {
        final Map<String, String> out = new TreeMap<String, String>();
        int count = Thread.activeCount();
        final Thread[] threads = new Thread[count];
        count = Thread.enumerate(threads);

        for (int i = 0; i < count; i++) {
            final String name = threads[i].getName();
            out.put(name, Base64.encodeBase64URLSafeString(name.getBytes()));
        }

        return out;
    }
}
