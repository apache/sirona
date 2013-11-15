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
package org.apache.sirona.repositories;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Repositories {
    public static Collection<Role> findByPrefixAndUnit(final String prefix, final Unit unit) {
        final Collection<Role> roles = new LinkedList<Role>();
        for (final Role role : Repository.INSTANCE.gauges()) {
            if (role.getName().startsWith(prefix)
                && unit.equals(role.getUnit())) {
                roles.add(role);
            }
        }
        return roles;
    }

    public static Collection<Role> findBySuffixAndUnit(final String suffix, final Unit unit) {
        final Collection<Role> roles = new LinkedList<Role>();
        for (final Role role : Repository.INSTANCE.gauges()) {
            if (role.getName().endsWith(suffix)
                && unit.equals(role.getUnit())) {
                roles.add(role);
            }
        }
        return roles;
    }

    public static Collection<String> names(final Collection<Role> membersGauges) {
        final Collection<String> names = new ArrayList<String>(membersGauges.size());
        for (final Role role : membersGauges) {
            names.add(role.getName());
        }
        return names;
    }

    private Repositories() {
        // no-op
    }
}
