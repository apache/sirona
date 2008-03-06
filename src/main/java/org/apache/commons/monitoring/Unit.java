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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Units allow monitored data to have get typed. A primary unit is the
 * finest unit for a data type. A primary unit can have derived units,
 * that represent the same data type, but with a scale factor.
 * A primary Unit is created with the {@link Unit#Unit(String)} constructor.
 * A derived Unit is created with the {@link Unit#Unit(String, Unit, long)} constructor.
 * <p>
 * A primary Unit maintains a Map of it's derived units. {@ Unit#getDerived()} can be
 * used to retrieve the complete list, and {@ Unit#getDerived(String)} to retrieve a
 * derived unit by it's name.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class Unit implements Comparable<Unit>
{
    /** Time based units */
    public static final Unit NANOS = new Unit( "ns" );
    public static final Unit MICROS = new Unit( "µs", NANOS, 1000 );
    public static final Unit MILLIS = new Unit( "ms", MICROS, 1000 );
    public static final Unit SECOND = new Unit( "s", MILLIS, 1000 );
    public static final Unit MINUTE = new Unit( "min", SECOND, 60 );
    public static final Unit HOUR = new Unit( "h", MINUTE, 60 );
    public static final Unit DAY = new Unit( "day", HOUR, 24 );

    /** Binary data units */
    public static final Unit BYTE = new Unit( "b" );
    public static final Unit KBYTE = new Unit( "Kb", BYTE, 1024 );
    public static final Unit MBYTE = new Unit( "Mb", KBYTE, 1024 );
    public static final Unit GBYTE = new Unit( "Gb", MBYTE, 1024 );


    /** unit for basic item counters & gauges */
    public static final Unit NONE = new Unit( "" );

    private final String name;

    private final long scale;

    private Unit primary;

    private List<Unit> derived;

    public Unit getDerived( String name )
    {
        for ( Unit unit : derived )
        {
            if (unit.name.equals( name ))
            {
                return unit;
            }
        }
        return null;
    }

    public List<Unit> getDerived()
    {
        return Collections.unmodifiableList( derived );
    }

    /**
     * Constructor for a primary unit
     * @param name
     */
    public Unit( String name )
    {
        this.name = name;
        this.primary = this;
        this.scale = 1;
        this.derived = new ArrayList<Unit>();
        this.derived.add( this );
    }

    /**
     * Constructor for a derived unit
     * @param name
     * @param derived the unit this unit is derived from
     * @param scale the scale factor to convert to derived unit
     */
    public Unit( String name, Unit derived, long scale )
    {
        this.name = name;
        this.primary = derived.isPrimary() ? derived : derived.getPrimary();
        this.scale = scale * derived.getScale();
        primary.derived.add( this );
        Collections.sort( primary.derived );
    }

    public String getName()
    {
        return name;
    }

    public long getScale()
    {
        return scale;
    }

    public boolean isPrimary()
    {
        return primary == this;
    }

    public boolean isCompatible( Unit unit )
    {
        return primary == unit.getPrimary();
    }

    public Unit getPrimary()
    {
        return this.primary;
    }

    public int compareTo( Unit o )
    {
        return scale < o.scale ? -1 : 1;
    }

    public String toString()
    {
        return name;
    }

}
