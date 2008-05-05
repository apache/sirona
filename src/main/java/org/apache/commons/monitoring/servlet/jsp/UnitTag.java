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

package org.apache.commons.monitoring.servlet.jsp;

import static org.apache.commons.monitoring.servlet.jsp.TagUtils.*;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.monitoring.Monitoring;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;

/**
 * A JSP tag to monitor JSP rendering performances
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class UnitTag
    extends TagSupport
{
    private String unit;

    private String id;

    private String name;

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag()
        throws JspException
    {
        StringBuffer out = new StringBuffer();
        out.append( "<select" );
        TagUtils.setAttribute( out, "name", name );
        TagUtils.setAttribute( out, "id", id );
        out.append( "'>" );
        Unit u = Unit.get( unit );
        for ( Unit derived : u.getPrimary().getDerived() )
        {
            out.append( "<option value='" );
            out.append( derived.getName() );
            out.append( "'" );
            if ( derived.equals( u ) )
            {
                out.append( " selected='selected'" );
            }
            out.append( ">" );
            out.append( derived.getName() );
            out.append( "</option>" );
        }
        out.append( "</select>" );

        try
        {
            pageContext.getOut().append( out.toString() );
        }
        catch ( IOException e )
        {
            throw new JspException( "UnitTag : " + e.getMessage() );
        }

        return EVAL_PAGE;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit( String unit )
    {
        this.unit = unit;
    }

    /**
     * @param id the id to set
     */
    public void setId( String id )
    {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
