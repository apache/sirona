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

import java.io.PrintWriter;

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Visitor;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractFilteredVisitor
    implements Visitor
{
    protected VisitorConfiguration configuration;

    protected PrintWriter writer;

    public AbstractFilteredVisitor( VisitorConfiguration configuration, PrintWriter writer )
    {
        super();
        this.configuration = configuration;
        this.writer = writer;
    }

    public void visit( Repository repository )
    {
        boolean first = true;
        for ( Monitor monitor : repository.getMonitors() )
        {
            if ( configuration.filter( monitor.getKey() ) )
            {
                if ( !first )
                {
                    next();
                    first = false;
                }
                monitor.accept( this );
            }
        }
    }

    public void visit( Monitor monitor )
    {
        boolean first = true;
        for ( Metric metric : monitor.getMetrics() )
        {
            if ( configuration.filter( metric.getRole() ) )
            {
                if ( !first )
                {
                    next();
                    first = false;
                }
                metric.accept( this );
            }
        }
    }

    protected void next()
    {
        // Nop
    }


}
