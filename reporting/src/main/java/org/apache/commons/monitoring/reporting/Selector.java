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

package org.apache.commons.monitoring.reporting;

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Repository;

/**
 * Retrieve a monitored data based on a path expression.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class Selector
{
    public final static String DEFAULT_SEPARATOR = "/";

    private MetricData value;

    private String role;

    private String name;

    private String category;

    private String subsystem;

    /**
     * Build a selector for the specified Path
     *
     * @param path
     */
    public Selector( String path )
    {
        this( path, DEFAULT_SEPARATOR );
    }

    /**
     * Build a selector for the specified Path
     *
     * @param path
     * @param separator path separator
     */
    public Selector( String path, String separator )
    {
        super();
        String[] tokens = path.split( separator );
        int i = tokens.length;
        if ( i < 3 )
        {
            throw new IllegalArgumentException( "Monitored data path must have at least 3 tokens" );
        }
        if ( i > 5 )
        {
            throw new IllegalArgumentException( "Monitored data path must have at most 5 tokens" );
        }
        this.value = MetricData.valueOf( tokens[--i] );
        this.role = tokens[--i];
        this.name = tokens[--i];
        this.category = ( i > 0 ? tokens[--i] : null );
        this.subsystem = ( i > 0 ? tokens[--i] : null );
    }

    public double getValue( Repository repository )
    {
        Metric metric = repository.getMonitor( name, category, subsystem ).getMetric( role );
        double d = value.value( metric );
        return d;
    }

}
