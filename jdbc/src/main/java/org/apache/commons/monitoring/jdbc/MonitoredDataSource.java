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

package org.apache.commons.monitoring.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Metric.Type;
import org.apache.commons.monitoring.stopwatches.DefaultStopWatch;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoredDataSource
    implements DataSource
{

    private final static Role OPEN_CONECTIONS = new Role( "open connections", Unit.UNARY, Type.GAUGE );

    private final static Role CONECTION_DURATION = new Role( "connection duration", Unit.Time.NANOSECOND, Type.COUNTER );

    /** delegate DataSource */
    private DataSource dataSource;

    /** dataSource name */
    private String dataSourceName = DataSource.class.getName();

    private Repository repository;

    private Monitor monitor;

    /**
     * Constructor
     * 
     * @param dataSource the datasource to monitor
     */
    public MonitoredDataSource( DataSource dataSource )
    {
        super();
        this.dataSource = dataSource;
    }

    /**
    *
    */
    public MonitoredDataSource()
    {
        super();
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName( String dataSourceName )
    {
        this.dataSourceName = dataSourceName;
    }

    /**
     * required
     * 
     * @param repository
     */
    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }

    public void init()
    {
        if ( monitor == null )
        {
            monitor = repository.getMonitor( dataSourceName, "jdbc" );
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection()
        throws SQLException
    {
        Connection connection = dataSource.getConnection();
        return monitor( connection );
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection( String username, String password )
        throws SQLException
    {
        Connection connection = dataSource.getConnection( username, password );
        return monitor( connection );
    }

    private Connection monitor( Connection connection )
    {
        // Computes the number of open connections and the connection duration
        final StopWatch stopWatch = new DefaultStopWatch( monitor, OPEN_CONECTIONS, CONECTION_DURATION );
        return new MonitoredConnection( connection, repository, new MonitoredConnection.ConnectionClosedCallBack()
        {
            public void onConnectionClosed()
            {
                stopWatch.stop();
            }
        } );
    }


    // --- delegate methods ---

    public int getLoginTimeout()
        throws SQLException
    {
        return dataSource.getLoginTimeout();
    }

    public PrintWriter getLogWriter()
        throws SQLException
    {
        return dataSource.getLogWriter();
    }

    public void setLoginTimeout( int seconds )
        throws SQLException
    {
        dataSource.setLoginTimeout( seconds );
    }

    public void setLogWriter( PrintWriter out )
        throws SQLException
    {
        dataSource.setLogWriter( out );
    }

    // --- jdbc4 ----

    public boolean isWrapperFor( Class<?> iface )
        throws SQLException
    {
        return dataSource.isWrapperFor( iface );
    }

    public <T> T unwrap( Class<T> iface )
        throws SQLException
    {
        return dataSource.unwrap( iface );
    }

}
