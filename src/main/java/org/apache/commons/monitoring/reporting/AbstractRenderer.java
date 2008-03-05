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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Render a collection of monitor for reporting
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRenderer
    implements Renderer
{
    public static final Renderer.Filter ALL = new Renderer.Filter()
    {
        public boolean render( Object object )
        {
            return true;
        }
    };

    public final void render( PrintWriter writer, Collection<Monitor> monitors )
    {
        render( writer, monitors, ALL );
    }

    public void render( PrintWriter writer, Collection<Monitor> monitors, Filter filter )
    {
        int count = 0;
        for ( Monitor monitor : monitors )
        {
            if ( filter.render( monitor ) )
            {
                if (count > 0)
                {
                    hasNext( writer, Monitor.class );
                }
                render( writer, monitor, filter );
                count ++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void hasNext( PrintWriter writer, Class type )
    {
        // Nop
    }

    protected void render( PrintWriter writer, Monitor monitor, Filter filter )
    {
        render( writer, monitor.getKey() );
        renderStatValues( writer, monitor, filter );
    }

    protected int renderStatValues( PrintWriter writer, Monitor monitor, Filter filter )
    {

        // Sort values by role to ensure predictable result
        List<StatValue> values = getOrderedStatValues( monitor, filter );
        for ( Iterator<StatValue> iterator = values.iterator(); iterator.hasNext(); )
        {
            StatValue value = (StatValue) iterator.next();
            render( writer, value );
            if (iterator.hasNext())
            {
                hasNext( writer, StatValue.class );
            }
        }
        return values.size();
    }

    protected List<StatValue> getOrderedStatValues( Monitor monitor, Filter filter )
    {
        List<StatValue> values = new LinkedList<StatValue>( monitor.getValues() );
        for ( Iterator<StatValue> iterator = values.iterator(); iterator.hasNext(); )
        {
            StatValue value = (StatValue) iterator.next();
            if ( ! filter.render( value ))
            {
                iterator.remove();
            }
        }
        Collections.sort( values, new Comparator<StatValue>()
        {
            public int compare( StatValue value1, StatValue value2 )
            {
                return value1.getRole().compareTo( value2.getRole() );
            }
        });
        return values;
    }

    protected abstract void render( PrintWriter writer, StatValue value );

    protected abstract void render( PrintWriter writer, Key key );
}
