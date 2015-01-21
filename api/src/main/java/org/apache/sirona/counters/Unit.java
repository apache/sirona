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

package org.apache.sirona.counters;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Units allow monitored data to have get typed. A primary unit is the
 * finest unit for a data type. A primary unit can have derived units,
 * that represent the same data type, but with a scale factor.
 * A primary Unit is created with the {@link Unit#Unit(String)} constructor.
 * A derived Unit is created with the {@link Unit#Unit(String, Unit, long)} constructor.
 * <p/>
 * A primary Unit maintains a Map of it's derived units. {@see Unit#getDerived()} can be
 * used to retrieve the complete list, and {@see Unit#getDerived(String)} to retrieve a
 * derived unit by it's name.
 *
 *
 */
public class Unit implements Comparable<Unit>, Serializable {
    private static final Map<String, Unit> UNITS = new ConcurrentHashMap<String, Unit>();

    /**
     * Time based units
     */
    public static class Time extends Unit {
        public static final Unit NANOSECOND = new Unit("ns");
        public static final Unit MICROSECOND = new Unit("us", NANOSECOND, 1000);
        public static final Unit MILLISECOND = new Unit("ms", MICROSECOND, 1000);
        public static final Unit SECOND = new Unit("s", MILLISECOND, 1000);
        public static final Unit MINUTE = new Unit("min", SECOND, 60);
        public static final Unit HOUR = new Unit("h", MINUTE, 60);
        public static final Unit DAY = new Unit("day", HOUR, 24);

        public Time(String name) {
            super(name);
        }

        public Time(String name, Unit derived, long scale) {
            super(name, derived, scale);
        }

    }

    /**
     * Binary data units
     */
    public static class Binary
            extends Unit {

        public static final Unit BYTE = new Unit("b");

        public static final Unit KBYTE = new Unit("Kb", BYTE, 1024);

        public static final Unit MBYTE = new Unit("Mb", KBYTE, 1024);

        public static final Unit GBYTE = new Unit("Gb", MBYTE, 1024);

        public Binary(String name) {
            super(name);
        }

        public Binary(String name, Unit derived, long scale) {
            super(name, derived, scale);
        }


    }

    /**
     * unit for basic item counters & gauges
     */
    // "BILLION" does not have same signification depending on country (10^12 or 10^9).
    // We use International system of unit names to avoid confusion
    // @see http://en.wikipedia.org/wiki/SI
    public static final Unit UNARY = new Unit("u");
    public static final Unit DECA = new Unit("*10", UNARY, 10);
    public static final Unit HECTO = new Unit("*100", DECA, 10);
    public static final Unit KILO = new Unit("*1000", HECTO, 10);
    public static final Unit MEGA = new Unit("*10^6", KILO, 1000);
    public static final Unit GIGA = new Unit("*10^9", MEGA, 1000);
    public static final Unit TERA = new Unit("*10^12", GIGA, 1000);

    private final String name;

    private final long scale;

    private Unit primary;


    public static Unit get(String name) {
        return UNITS.get(name);
    }

    /**
     * Constructor for a primary unit
     *
     * @param name
     */
    public Unit(String name) {
        this.name = name;
        this.primary = this;
        this.scale = 1;
        UNITS.put(name, this);
    }

    /**
     * Constructor for a derived unit
     *
     * @param name
     * @param derived the unit this unit is derived from
     * @param scale   the scale factor to convert to derived unit
     */
    public Unit(String name, Unit derived, long scale) {
        this.name = name;
        this.primary = derived.isPrimary() ? derived : derived.getPrimary();
        this.scale = scale * derived.getScale();
        UNITS.put(name, this);
    }

    public String getName() {
        return name;
    }

    public long getScale() {
        return scale;
    }

    /**
     * Convert value from unit to this unit (if conpatible)
     *
     * @param value value to convert
     * @param unit  unit of value
     * @return value converted to this unit
     */
    public double convert(final double value, final Unit unit) {
        if (unit == this) {
            return value;
        }
        if (!isCompatible(unit)) {
            throw new IllegalArgumentException("unit " + name + " is incompatible with unit " + unit.name);
        }
        return value * unit.getScale() / scale;
    }

    public boolean isPrimary() {
        return primary == this;
    }

    public boolean isCompatible(Unit unit) {
        return primary == unit.getPrimary();
    }

    public Unit getPrimary() {
        return this.primary;
    }

    public int compareTo(Unit o) {
        return scale < o.scale ? -1 : 1;
    }

    public String toString() {
        return name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Unit other = (Unit) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
