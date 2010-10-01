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

import java.util.Collection;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Visitor;

public abstract class AbstractVisitor
    implements Visitor
{
    private RepositoryFilter filter = FULL;

    public void setFilter( RepositoryFilter filter )
    {
        this.filter = filter;
    }

    protected abstract void monitorEnd( String name );

    protected abstract void monitorStart( Monitor monitor );

    protected abstract void gaugeStart( String name );

    protected abstract void gaugeEnd( String name );

    protected abstract void counterStart( String name );

    protected abstract void counterEnd( String name );

    protected abstract void repositoryEnd();

    protected abstract void repositoryStart();

    protected abstract void attribute( String name, double value );

    protected abstract void attribute( String name, String value );

    public final void visit( Repository repository )
    {
        repositoryStart();
        doVisit( repository );
        repositoryEnd();
    }

    protected abstract void doVisit( Repository repository );

    protected Collection<Monitor> getMonitors( Repository repository )
    {
        return repository.getMonitors();
    }

    public final void visit( Monitor monitor )
    {
        Monitor.Key key = monitor.getKey();
        String name = key.getName();
        monitorStart( monitor );
        doVisit( monitor );
        monitorEnd( name );
    }

    protected abstract void doVisit( Monitor monitor );

    protected Collection<Metric> getMetrics( Monitor monitor )
    {
        return monitor.getMetrics();
    }

    public final void visit( Gauge gauge )
    {
        String name = gauge.getRole().getName();
        gaugeStart( name );
        attribute( "unit", gauge.getUnit().getName() );
        attribute( "value", gauge.getValue() );
        attribute( "max", gauge.getMax() );
        attribute( "min", gauge.getMin() );
        attribute( "hits", gauge.getHits() );
        attribute( "standardDeviation", gauge.getStandardDeviation() );
        attribute( "variance", gauge.getVariance() );
        attribute( "mean", gauge.getMean() );
        attribute( "geometricMean", gauge.getGeometricMean() );
        attribute( "sumOfLogs", gauge.getSumOfLogs() );
        attribute( "sumOfSquares", gauge.getSumOfSquares() );
        gaugeEnd( name );
    }

    public final void visit( Counter counter )
    {
        String name = counter.getRole().getName();
        counterStart( name );
        attribute( "unit", counter.getUnit().getName() );
        attribute( "sum", counter.getSum() );
        attribute( "max", counter.getMax() );
        attribute( "min", counter.getMin() );
        attribute( "hits", counter.getHits() );
        attribute( "standardDeviation", counter.getStandardDeviation() );
        attribute( "variance", counter.getVariance() );
        attribute( "mean", counter.getMean() );
        attribute( "geometricMean", counter.getGeometricMean() );
        attribute( "sumOfLogs", counter.getSumOfLogs() );
        attribute( "sumOfSquares", counter.getSumOfSquares() );
        counterEnd( name );
    }

    private static final RepositoryFilter FULL = new RepositoryFilter()
    {

        public boolean filter( Monitor monitor )
        {
            return true;
        }

        public boolean filter( Metric metric )
        {
            return true;
        }

    };
}