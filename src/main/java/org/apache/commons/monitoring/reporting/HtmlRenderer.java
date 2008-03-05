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
import java.util.Collection;
import java.util.List;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HtmlRenderer
    extends AbstractRenderer
{

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.PrintWriter,
     * java.util.Collection,
     * org.apache.commons.monitoring.reporting.Renderer.Filter)
     */
    @Override
    public void render( PrintWriter writer, Collection<Monitor> monitors, Filter filter )
    {
        documentHead( writer );
        tableStartTag( writer );
        tableHead( writer, monitors, filter );
        super.render( writer, monitors, filter );
        writer.append( "</tr></tbody>" );
        tableEndTag( writer );
        documentFoot( writer );
    }

    /**
     * @param writer
     */
    protected void tableStartTag( PrintWriter writer )
    {
        writer.append( "<table>" );
    }

    /**
     * @param writer
     */
    protected void tableEndTag( PrintWriter writer )
    {
        writer.append( "</table>" );
    }

    /**
     * @param writer
     */
    protected void documentHead( PrintWriter writer )
    {
        writer.append( "<html><body>" );
    }

    protected void tableHead( PrintWriter writer, Collection<Monitor> monitors, Filter filter )
    {
        writer.append( "<thead><tr><th rowspan='2'>name</th><th rowspan='2'>category</th><th rowspan='2'>subsystem</th>" );
        Monitor monitor = monitors.iterator().next();
        List<StatValue> values = getOrderedStatValues( monitor, filter );
        for ( StatValue value : values )
        {
            if ( value instanceof Counter )
            {
                writer.append( "<th colspan='7'>" );
            }
            else
            {
                writer.append( "<th colspan='5'>" );
            }
            writer.append( value.getRole() );
            if ( value.getUnit() != null )
            {
                writer.append( " (" );
                writer.append( value.getUnit() );
                writer.append( ")" );
            }
            writer.append( "</th>" );
        }
        writer.append( "</tr>" );
        writer.append( "<tr>" );
        for ( StatValue value : values )
        {
            writer.append( "<th>value</th><th>min</th><th>max</th><th>mean</th><th>dev.</th>" );
            if ( value instanceof Counter )
            {
                writer.append( "<th>sum</th><th>hits</th>" );
            }
        }
        writer.append( "</tr></thead><tbody><tr>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.PrintWriter,
     * org.apache.commons.monitoring.StatValue)
     */
    @Override
    protected void render( PrintWriter writer, StatValue value )
    {
        writer.append( "<td title='" );
        writer.append( value.getRole() );
        writer.append( "'>" );
        writer.append( String.valueOf( value.get() ) );
        writer.append( "</td><td>" );
        writer.append( String.valueOf( value.getMin() ) );
        writer.append( "</td><td>" );
        writer.append( String.valueOf( value.getMax() ) );
        writer.append( "</td><td>" );
        writer.append( String.valueOf( value.getMean() ) );
        writer.append( "</td><td>" );
        writer.append( String.valueOf( value.getStandardDeviation() ) );
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            writer.append( "</td><td>" );
            writer.append( String.valueOf( counter.getSum() ) );
            writer.append( "</td><td>" );
            writer.append( String.valueOf( counter.getHits() ) );
        }
        writer.append( "</td>" );

    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(java.io.PrintWriter,
     * org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    protected void render( PrintWriter writer, Key key )
    {
        writer.append( "<td>" );
        writer.append( key.getName() );
        writer.append( "</td><td>" );
        if ( key.getCategory() != null )
        {
            writer.append( key.getCategory() );
        }
        writer.append( "</td><td>" );
        if ( key.getSubsystem() != null )
        {
            writer.append( key.getSubsystem() );
        }
        writer.append( "</td>" );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#hasNext(java.io.PrintWriter,
     * java.lang.Class)
     */
    @Override
    protected void hasNext( PrintWriter writer, Class type )
    {
        if ( type == Monitor.class )
        {
            writer.append( "</tr><tr>" );
        }
    }

    /**
     * @param writer
     */
    protected void documentFoot( PrintWriter writer )
    {
        writer.append( "</body></html>" );
    }

}
