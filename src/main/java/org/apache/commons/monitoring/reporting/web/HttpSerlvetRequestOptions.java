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

package org.apache.commons.monitoring.reporting.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Monitor.Key;
import org.apache.commons.monitoring.reporting.OptionsSupport;

public class HttpSerlvetRequestOptions
    extends OptionsSupport
{
    protected final HttpServletRequest request;

    protected List<String> roles;

    protected List<String> categories;

    protected List<String> subsystems;

    /**
     * @param request
     */
    public HttpSerlvetRequestOptions( HttpServletRequest request )
    {
        this.request = request;
        this.roles = new ArrayList<String>();
        // Roles set as "role=x&role=y"
        String[] values = request.getParameterValues( "role" );
        if ( values != null )
        {
            roles.addAll( Arrays.asList( values ) );
        }
        // Roles set as "roles=x,y"
        String value = request.getParameter( "roles" );
        if ( value != null )
        {
            roles.addAll( Arrays.asList( value.split( "," ) ) );
        }

        values = request.getParameterValues( "category" );
        categories = values != null ? Arrays.asList( values ) : Collections.<String> emptyList();
        values = request.getParameterValues( "subsystem" );
        subsystems = values != null ? Arrays.asList( values ) : Collections.<String> emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean renderRole( Role role )
    {
        return roles.isEmpty() ? true : roles.contains( role.getName() );
    }

    @Override
    public boolean render( Monitor monitor )
    {
        Key key = monitor.getKey();
        return ( categories.isEmpty() || categories.contains( key.getCategory() ) )
            && ( subsystems.isEmpty() || subsystems.contains( key.getSubsystem() ) );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean render( Role role, String attribute )
    {
        // attribute hidden using "myrole.x=false"
        String param = request.getParameter( role.getName() + "." + attribute );
        if (param != null)
        {
            return Boolean.parseBoolean( param );
        }
        // attribute selected using "myrole.columns=x,y,z"
        String columns = request.getParameter( role.getName() + ".columns" );
        if ( columns == null )
        {
            return true;
        }
        return columns.indexOf( attribute ) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Unit unitFor( Role role )
    {
        String unitName = request.getParameter( role.getName() + ".unit" );
        if ( unitName != null )
        {
            if ( role.getUnit() != null )
            {
                Unit unit = role.getUnit().getDerived( unitName );
                if ( unit != null )
                {
                    return unit;
                }
            }
        }
        return role.getUnit();
    }
}