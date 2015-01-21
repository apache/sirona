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

package org.apache.sirona;

import org.apache.sirona.counters.Unit;

import java.io.Serializable;

/**
 * As a monitored resource may have multipe Metrics, each one has a dedicated 'role' that
 * defines the type of data or the monitored aspect it handles.
 *
 *
 */
public class Role implements Comparable<Role>, Serializable {
    public static final Role WEB = new Role("web", Unit.Time.NANOSECOND);
    public static final Role JSP = new Role("jsp", Unit.Time.NANOSECOND);
    public static final Role JDBC = new Role("jdbc", Unit.Time.NANOSECOND);
    public static final Role PERFORMANCES = new Role("performances", Unit.Time.NANOSECOND);
    public static final Role FAILURES = new Role("failures", Unit.UNARY);

    private final String name;
    private final Unit unit;

    public Role(String name, Unit unit) {
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
    
    public int hashCode() {
        return name.hashCode();
    }

    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Role role = Role.class.cast(o);
        return name.equals(role.name);

    }

    /**
     * {@inheritDoc}
     */
    
    public int compareTo(final Role o) {
        return name.compareTo(o.name);
    }

    
    public String toString() {
        return "Role{" + "name='" + name + '\'' + ", unit=" + unit + '}';
    }
}
