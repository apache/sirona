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

package org.apache.commons.monitoring.monitors;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Visitable;
import org.apache.commons.monitoring.counter.Counter;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A <code>Monitor</code> is an abstraction of some application resource that is instrumented with a set of indicators
 * (Gauges or Counters).
 * <p/>
 * A Monitor is identified by its Key, that MUST be unique in the application. To ensure this, the Key class defines the
 * monitor identifier as a combination of name, subsystem and category.
 * <p/>
 * The <tt>name</tt> is the human-readable representation of the "resource" beeing monitored. A typical use is the fully
 * qualified class name + method signature, or the HTTP request path.
 * <p/>
 * The <tt>category</tt> is a grouping attribute to reflect the application layering. Typically for JEE application, you
 * will set category to the N-tier layer beeing monitored ("servlet", "service", "persistence").
 * <p/>
 * You are free to use more complex Key types, by simple subclassing the Key class and providing the adequate
 * equals()/hasCode() methods.
 * <p/>
 * The Counters / Gauges used to store monitored application state are retrieved based on a "role" String. The monitor
 * can handle as many Metrics as needed, until any of them has a dedicated role. This allows to easily extend the
 * monitor by registering custom Metrics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Monitor extends Visitable {
    /**
     * @return the monitor key
     */
    Key getKey();

    /**
     * Retrieve a Counter
     *
     * @param role a unique identifier for a Counter in the monitor
     * @return the Counter
     */
    Counter getCounter(String role);

    /**
     * Retrieve or create a Counter in the monitor
     *
     * @param role the Counter role in the monitor
     * @return the Counter
     */
    Counter getCounter(Role role);

    /**
     * @return an unmodifiable collection of registered Metrics roles
     */
    Collection<Role> getRoles();

    /**
     * @return an unmodifiable collection of registered Metrics
     */
    Collection<Counter> getCounters();

    /**
     * Reset all Metrics (don't remove them)
     */
    void reset();

    AtomicInteger currentConcurrency();

    void updateConcurrency(int concurrency);

    int getMaxConcurrency();

    /**
     * Identifier class for Monitors.
     * <p/>
     * The name is expected to define a resource or point to some precise code fragment.
     * The category may be used to declare the application technical layer where the monitor
     * is registered. The subsystem may be used to declare the logical part the monitored
     * element belong to.
     * <p/>
     * User may extend this class to define custom keys, just be aware to override the {@see #equals())
     * and {@see #hashCode()} methods to ensure unicity of the Keys.
     */
    public static class Key {
        public final static String DEFAULT = "default";

        private final String name;

        private final String category;

        public Key(final String name, final String category) {
            super();
            if (name == null) {
                throw new IllegalArgumentException("A name must be provided");
            }
            this.name = name;
            this.category = category != null ? category : DEFAULT;
        }

        @Override
        public String toString() {
            final StringBuilder stb = new StringBuilder();
            stb.append("name=");
            stb.append(name);
            if (category != null) {
                stb.append("\ncategory=");
                stb.append(category);
            }
            return stb.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Key.class != o.getClass()) {
                return false;
            }

            final Key key = Key.class.cast(o);
            return category.equals(key.category) && name.equals(key.name);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + category.hashCode();
            return result;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }
    }
}
