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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Role<T extends StatValue> implements Comparable<Role>
{
    private String name;

    private Unit unit;

    private static final Map<String, Role> ROLES = new ConcurrentHashMap<String, Role>();

    public static Role<? extends StatValue> getRole( String name )
    {
        return ROLES.get( name );
    }

    public Role( String name, Unit unit )
    {
        super();
        if (name == null)
        {
            throw new IllegalArgumentException( "A role name is required" );
        }
        if (unit == null)
        {
            throw new IllegalArgumentException( "A role unit is required" );
        }
        this.name = name;
        this.unit = unit;
        ROLES.put( name, this );
    }

    /**
     * @return the role
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the unit
     */
    public Unit getUnit()
    {
        return unit;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( unit == null ) ? 0 : unit.hashCode() );
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        final Role other = (Role) obj;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        if ( unit == null )
        {
            if ( other.unit != null )
                return false;
        }
        else if ( !unit.equals( other.unit ) )
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo( Role o )
    {
        return name.compareTo( o.name );
    }

}
