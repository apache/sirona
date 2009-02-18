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

package org.apache.commons.monitoring;

import static org.apache.commons.monitoring.Unit.Time.MILLISECOND;

import java.util.Collection;
import java.util.EventListener;

/**
 * A <code>Monitor</code> is an abstraction of some application resource that is instrumented with a set of indicators
 * (Gauges or Counters).
 * <p>
 * A Monitor is identified by its Key, that MUST be unique in the application. To ensure this, the Key class defines the
 * monitor identifier as a combination of name, subsystem and category.
 * <p>
 * The <tt>name</tt> is the human-readable representation of the "resource" beeing monitored. A typical use is the fully
 * qualified class name + method signature, or the HTTP request path.
 * <p>
 * The <tt>category</tt> is a grouping attribute to reflect the application layering. Typically for JEE application, you
 * will set category to the N-tier layer beeing monitored ("servlet", "service", "persistence").
 * <p>
 * The <tt>subsystem</tt> is a logical grouping, by use-cases. "account", and "user" can be used as subsystem for the
 * application account and user management dedicated components.
 * <p>
 * You are free to use more complex Key types, by simple subclassing the Key class and providing the adequate
 * equals()/hasCode() methods.
 * <p>
 * The Counters / Gauges used to store monitored application state are retrieved based on a "role" String. The monitor
 * can handle as many Metrics as needed, until any of them has a dedicated role. This allows to easily extend the
 * monitor by registering custom Metrics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Monitor
{
    // --- Predefined roles -------------------------------------------------------------------

    /** Predefined role key for code performances */
    Role PERFORMANCES = new Role( "performances", MILLISECOND, Metric.Type.COUNTER );

    /** Predefined role key for cpu time */
    Role CPU = new Role( "cpu", MILLISECOND, Metric.Type.COUNTER );

    /** Predefined role for multi-thread concurrency */
    Role CONCURRENCY = new Role( "concurrency", Unit.UNARY, Metric.Type.GAUGE );

    /** Predefined Role for the invocation failure counter */
    Role FAILURES = new Role( "failures", Unit.UNARY, Metric.Type.COUNTER );


    /**
     * @return the monitor key
     */
    Key getKey();

    /**
     * Retrieve a Counter
     *
     * @param role a unique identifier for a Counter in the monitor
     * @param unit the data unit to count
     * @return the Counter
     */
    Counter getCounter( String role );

    /**
     * Retrieve or create a Counter in the monitor
     *
     * @param role the Counter role in the monitor
     * @return the Counter
     */
    Counter getCounter( Role role );

    /**
     * Retrieve a Gauge in the monitor
     *
     * @param role a unique identifier for a Gauge in the monitor
     * @return the Gauge
     */
    Gauge getGauge( String role );

    /**
     * Retrieve or create a Gauge in the monitor
     *
     * @param role the gauge role in the monitor
     * @return the Gauge
     */
    Gauge getGauge( Role role );

    /**
     * Retrieve a Metric.
     *
     * @param role a unique identifier for a Metric in the monitor
     * @return the Metric
     */
    Metric getMetric( String role );

    /**
     * Retrieve a Metric in the monitor
     *
     * @param role the Metric role in the monitor
     * @return the Metric
     */
    Metric getMetric( Role role );

    /**
     * @return an unmodifiable collection of registered Metrics roles
     */
    Collection<Role> getRoles();

    /**
     * @return an unmodifiable collection of registered Metrics
     */
    Collection<Metric> getMetrics();

    /**
     * Reset all Metrics (don't remove them)
     */
    void reset();

    /**
     * Identifier class for Monitors.
     * <p>
     * The name is expected to define a resource or point to some precise code fragment.
     * The category may be used to declare the application technical layer where the monitor
     * is registered. The subsystem may be used to declare the logical part the monitored
     * element belong to.
     * <p>
     * User may extend this class to define custom keys, just be aware to override the {@ #equals())
     * and {@ #hashCode()} methods to ensure unicity of the Keys.
     */
    public static class Key
    {
        public final static String DEFAULT = "default";

        private final String name;

        private final String category;

        private final String subsystem;

        public Key( String name, String category, String subsystem )
        {
            super();
            if (name == null)
            {
                throw new IllegalArgumentException( "A name must be provided" );
            }
            this.name = name;
            this.category = category != null ? category : DEFAULT;
            this.subsystem = subsystem != null ? subsystem : DEFAULT;;
        }

        @Override
        public String toString()
        {
            StringBuilder stb = new StringBuilder();
            stb.append( "name=" );
            stb.append( name );
            if ( category != null )
            {
                stb.append( "\ncategory=" );
                stb.append( category );
            }
            if ( subsystem != null )
            {
                stb.append( "\nsubsystem=" );
                stb.append( subsystem );
            }
            return stb.toString();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( category == null ) ? 0 : category.hashCode() );
            result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
            result = prime * result + ( ( subsystem == null ) ? 0 : subsystem.hashCode() );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( getClass() != obj.getClass() )
            {
                return false;
            }
            final Key other = (Key) obj;
            if ( category == null )
            {
                if ( other.category != null )
                {
                    return false;
                }
            }
            else if ( !category.equals( other.category ) )
            {
                return false;
            }
            if ( name == null )
            {
                if ( other.name != null )
                {
                    return false;
                }
            }
            else if ( !name.equals( other.name ) )
            {
                return false;
            }
            if ( subsystem == null )
            {
                if ( other.subsystem != null )
                {
                    return false;
                }
            }
            else if ( !subsystem.equals( other.subsystem ) )
            {
                return false;
            }
            return true;
        }

        public String getName()
        {
            return name;
        }

        public String getCategory()
        {
            return category;
        }

        public String getSubsystem()
        {
            return subsystem;
        }

    }


    /**
     * Listener interface to get notified on montor events
     */
    public interface Listener
    extends EventListener
    {
        void onMetricRegistered( Monitor.Observable monitor, Metric metric );
    }

    /**
     * A monitor that support the Observer pattern.
     */
    public interface Observable extends Monitor
    {
        /**
         * @param listener listener to get registered
         */
        void addListener( Listener listener );

        /**
         * @param listener listener to get removed
         */
        void removeListener( Listener listener );
    }


}
