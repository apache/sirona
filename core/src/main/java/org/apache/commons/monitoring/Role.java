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

import org.apache.commons.monitoring.counter.Unit;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.monitoring.counter.Unit.Time.NANOSECOND;

/**
 * As a monitored resource may have multipe Metrics, each one has a dedicated 'role' that
 * defines the type of data or the monitored aspect it handles.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class Role implements Comparable<Role> {
    private static final ConcurrentMap<String, Role> ROLES = new ConcurrentHashMap<String, Role>();

    public static final Role PERFORMANCES = new Role("performances", NANOSECOND);
    public static final Role FAILURES = new Role("failures", Unit.UNARY);

    private String name;
    private Unit unit;

    public static Role getRole(String name) {
        return ROLES.get(name);

    }

    public static Collection<Role> getRoles() {
        return Collections.unmodifiableCollection(ROLES.values());
    }

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
        final Role old = ROLES.putIfAbsent(name, this);
        if (old != null) {
            if (!unit.equals(old.unit)) {
                throw new IllegalStateException("A role already exists with this name but distinct unit");
            }
        }
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
    public int compareTo(Role o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
