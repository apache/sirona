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

package org.apache.sirona.reporting.web.counters;

import org.apache.sirona.counters.Counter;

import java.io.Serializable;

/**
 * @since 0.3
 */
public class KeyInfo
    implements Serializable
{

    private final String name;

    private final String roleName;

    private String unitName;

    public KeyInfo( Counter.Key key )
    {
        this.name = key.getName();

        this.roleName = key.getRole().getName();

        this.unitName = key.getRole().getUnit().getName();
    }

    public String getName()
    {
        return name;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public String getUnitName()
    {
        return unitName;
    }

    public void setUnitName( String unitName )
    {
        this.unitName = unitName;
    }

    public KeyInfo unitName( String unitName )
    {
        this.unitName = unitName;
        return this;
    }

    @Override
    public String toString()
    {
        return "KeyInfo{" +
            "name='" + name + '\'' +
            ", roleName='" + roleName + '\'' +
            ", unitName='" + unitName + '\'' +
            '}';
    }
}
