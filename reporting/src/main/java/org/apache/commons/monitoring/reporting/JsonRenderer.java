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

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class JsonRenderer
    extends AbstractFilteredVisitor
{
    public JsonRenderer( VisitorConfiguration configuration, PrintWriter writer )
    {
        super( configuration, writer );
    }

    public void visit( Repository repository )
    {
        writer.append( "repository: {" );
        super.visit( repository );
        writer.append( "}\n" );
    }

    public void visit( Monitor monitor )
    {
        Monitor.Key key = monitor.getKey();
        writer.append( "  \"" );
        writer.append( key.getName() );
        writer.append( "\": { category:\"" );
        writer.append( key.getCategory() );
        writer.append( "\", subsystem:\"" );
        writer.append( key.getSubsystem() );
        writer.append( "\",\n" );
        super.visit( monitor );
        writer.append( "  }\n" );
    }

    @Override
    protected void next()
    {
        writer.append( "," );
    }

    public void visit( Gauge gauge )
    {
        writer.append( "    \"" ).append( gauge.getRole().getName() );
        writer.append( "\": { type:\"gauge\", unit:\"" ).append( gauge.getUnit().getName() );
        writer.append( "\", value:\"" );
        writer.append( configuration.format( gauge.getValue() ) );
        writer.append( "\"\n             max:\"" );
        writer.append( configuration.format( gauge.getMax() ) );
        writer.append( "\", min:\"" );
        writer.append( configuration.format( gauge.getMin() ) );
        writer.append( "\", hits:\"" );
        writer.append( configuration.format( gauge.getHits() ) );
        writer.append( "\", standardDeviation:\"" );
        writer.append( configuration.format( gauge.getStandardDeviation() ) );
        writer.append( "\", variance:\"" );
        writer.append( configuration.format( gauge.getVariance() ) );
        writer.append( "\", mean:\"" );
        writer.append( configuration.format( gauge.getMean() ) );
        writer.append( "\", geometricMean:\"" );
        writer.append( configuration.format( gauge.getGeometricMean() ) );
        writer.append( "\", sumOfLogs:\"" );
        writer.append( configuration.format( gauge.getSumOfLogs() ) );
        writer.append( "\", somOfSquares:\"" );
        writer.append( configuration.format( gauge.getSumOfSquares() ) );
        writer.append( "\"}\n" );
    }

    public void visit( Counter counter )
    {
        writer.append( "    \"" ).append( counter.getRole().getName() );
        writer.append( "\": { type:\"counter\", unit:\"" ).append( counter.getUnit().getName() );
        writer.append( "\", sum:\"" );
        writer.append( configuration.format( counter.getSum() ) );
        writer.append( "\"\n             max:\"" );
        writer.append( configuration.format( counter.getMax() ) );
        writer.append( "\", min:\"" );
        writer.append( configuration.format( counter.getMin() ) );
        writer.append( "\", hits:\"" );
        writer.append( configuration.format( counter.getHits() ) );
        writer.append( "\", standardDeviation:\"" );
        writer.append( configuration.format( counter.getStandardDeviation() ) );
        writer.append( "\", variance:\"" );
        writer.append( configuration.format( counter.getVariance() ) );
        writer.append( "\", mean:\"" );
        writer.append( configuration.format( counter.getMean() ) );
        writer.append( "\", geometricMean:\"" );
        writer.append( configuration.format( counter.getGeometricMean() ) );
        writer.append( "\", sumOfLogs:\"" );
        writer.append( configuration.format( counter.getSumOfLogs() ) );
        writer.append( "\", somOfSquares:\"" );
        writer.append( configuration.format( counter.getSumOfSquares() ) );
        writer.append( "\"}\n" );
    }

}