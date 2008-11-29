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

package org.apache.commons.monitoring.instrumentation.servlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Select the category to use for a requested URI by searching a matching pattern
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CategorizedMonitoringFilter
    extends MonitoringFilter
    implements Filter
{
    /**
     * Ordered Map of patterns that defines categories. First pattern matching the requested URI define the category
     */
    Map<Pattern, String> categories = new LinkedHashMap<Pattern, String>();

    public void registerCategoryForPattern( String pattern, String category )
    {
        categories.put( Pattern.compile( pattern ), category );
    }

    @Override
    protected String getCategory( String uri )
    {
        for ( Map.Entry<Pattern, String> entry : categories.entrySet() )
        {
            Pattern pattern = entry.getKey();
            if ( pattern.matcher( uri ).matches() )
            {
                return entry.getValue();
            }
        }
        return super.getCategory( uri );
    }

    @Override
    public void init( FilterConfig config )
        throws ServletException
    {
        super.init( config );
        // TODO get patterns from configuration
    }
}
