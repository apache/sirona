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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;

public class FormattingVisitor
    extends AbstractVisitor
{
    protected PrintWriter writer;

    private NumberFormat numberFormat;

    private Format format;

    public FormattingVisitor( Format format, PrintWriter writer )
    {
        super();
        this.format = format;
        this.writer = writer;
        this.numberFormat = DecimalFormat.getNumberInstance( Locale.US );
        this.numberFormat.setMinimumFractionDigits( 1 );
    }

    protected void doVisit( Repository repository )
    {
        boolean first = true;
        for ( Monitor monitor : getMonitors( repository ) )
        {
            if ( !first )
            {
                separator();
            }
            first = false;
            monitor.accept( this );
        }
    }

    protected void doVisit( Monitor monitor )
    {
        boolean first = true;
        for ( Metric metric : getMetrics( monitor ) )
        {
            if ( !first )
            {
                separator();
            }
            first = false;
            metric.accept( this );
        }
    }

    protected void attribute( String name, double value )
    {
        attribute( name, format( value ) );
    }

    protected String format( double value )
    {
        String s = numberFormat.format( value );
        if ( "\uFFFD".equals( s ) )
        {
            // Locale may have no DecimalFormatSymbols.NaN (set to "\uFFFD" (REPLACE_CHAR))
            s = "NaN";
        }
        return s;
    }

    @Override
    protected void repositoryStart()
    {
        format.repositoryStart( writer );
    }

    @Override
    protected void repositoryEnd()
    {
        format.repositoryEnd( writer );
    }
    
    protected void attribute( String name, String value )
    {
        format.attribute( writer, name, value );
    }

    @Override
    protected void counterEnd( String name )
    {
        format.counterEnd( writer, name );
    }

    @Override
    protected void counterStart( String name )
    {
        format.counterStart( writer, name );
    }

    protected void escape( String string )
    {
        format.escape( writer, string );
    }

    @Override
    protected void gaugeEnd( String name )
    {
        format.gaugeEnd( writer, name );
    }

    @Override
    protected void gaugeStart( String name )
    {
        format.gaugeStart( writer, name );
    }

    @Override
    protected void monitorStart( Monitor monitor )
    {
        format.monitorStart( writer, monitor );
    }

    @Override
    protected void monitorEnd( String name )
    {
        format.monitorEnd( writer, name );
    }

    protected void separator()
    {
        format.separator( writer );
    }


}