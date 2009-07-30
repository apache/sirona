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

package org.apache.commons.monitoring.instrumentation.servlet.jsp;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.monitoring.Repository;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class TagUtils
{
    /** Maps JSP scope names to PageContext constants */
    private static final Map<String, Integer> scopes = new HashMap<String, Integer>();

    static
    {
        scopes.put( "page", new Integer( PageContext.PAGE_SCOPE ) );
        scopes.put( "request", new Integer( PageContext.REQUEST_SCOPE ) );
        scopes.put( "session", new Integer( PageContext.SESSION_SCOPE ) );
        scopes.put( "application", new Integer( PageContext.APPLICATION_SCOPE ) );
    }

    /**
     * Converts the scope name into its corresponding PageContext constant.
     *
     * @param scopeName Can be "page", "request", "session", or "application".
     * @return The constant representing the scope (ie. PageContext.*_SCOPE).
     */
    public static int getScope( String scopeName )
    {
        return scopes.get( scopeName.toLowerCase() );
    }

    /**
     * @param out
     * @param name TODO
     * @param value TODO
     */
    public static void setAttribute( StringBuffer out, String name, String value )
    {
        if ( value != null )
        {
            out.append( " " ).append( name ).append( "='" ).append( value ).append( "'" );
        }
    }

    public static Repository getRepository( PageContext pageContext, String key)
        throws JspException
    {
        if ( key != null )
        {
            Repository repo = (Repository) pageContext.getAttribute( key );
            if ( repo == null )
            {
                throw new JspException( "No repository on pageContext for key " + key );
            }
        }
        return null; //Monitoring.getRepository();
    }
}
