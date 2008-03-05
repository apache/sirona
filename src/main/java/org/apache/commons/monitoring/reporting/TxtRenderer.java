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
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * A simple TXT renderer, typically to dump monitoring status in a log file
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class TxtRenderer
    extends AbstractRenderer
{
    private static final String HR = "\n--------------------------------------------------------------------------------\n";

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(org.apache.commons.monitoring.StatValue, java.lang.String)
     */
    @Override
    public void render( PrintWriter writer, StatValue value )
    {
        writer.append( value.getRole() );
        writer.append( "\n    value  : " );
        writer.append( String.valueOf( value.get() ) );
        writer.append( "\n    min    : " );
        writer.append( String.valueOf( value.getMin() ) );
        writer.append( "\n    max    : " );
        writer.append( String.valueOf( value.getMax() ) );
        writer.append( "\n    mean   : " );
        writer.append( String.valueOf( value.getMean() ) );
        writer.append( "\n    stdDev : " );
        writer.append( String.valueOf( value.getStandardDeviation() ) );
        if ( value instanceof Counter )
        {
            Counter counter = (Counter) value;
            writer.append( "\n    total  : " );
            writer.append( String.valueOf( counter.getSum() ) );
            writer.append( "\n    hits   : " );
            writer.append( String.valueOf( counter.getHits() ) );
        }
        writer.append( "\n" );
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    public void render( PrintWriter writer, Key key )
    {
        writer.append( HR );
        writer.append( key.toString() );
        writer.append( HR );
    }

}
