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

package org.apache.commons.monitoring;

import org.apache.commons.monitoring.counters.Unit;

import static org.apache.commons.monitoring.counters.Unit.Time.NANOSECOND;

/**
 * As a monitored resource may have multipe Metrics, each one has a dedicated 'role' that
 * defines the type of data or the monitored aspect it handles.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class Role implements Comparable<Role> {
    public static final Role WEB = new Role("web", NANOSECOND);
    public static final Role JSP = new Role("jsp", NANOSECOND);
    public static final Role JDBC = new Role("jdbc", NANOSECOND);
    public static final Role PERFORMANCES = new Role("performances", NANOSECOND);
    public static final Role FAILURES = new Role("failures", Unit.UNARY);

    private String name;
    private Unit unit;

    public Role(String name, Unit unit) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("A role name is required");
        }
        if (unit == null) {
            throw new IllegalArgumentException("A role unit is required");
        }
        this.name = name;
        this.unit = unit;
    }

    /**
     * @return the role
     */
    public String getName() {
        return name;
    }

    /**
     * @return the unit
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Role other = (Role) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Role o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
