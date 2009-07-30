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

import org.apache.commons.monitoring.Repository;


/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoredDataSource extends AbstractMonitoredDataSource
    implements DataSource
{
    /**
     * Constructor
     * 
     * @param dataSource the datasource to monitor
     */
    public MonitoredDataSource( DataSource dataSource, Repository repository )
    {
        super( dataSource, repository );
    }

    public MonitoredDataSource()
    {
        super();
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection()
        throws SQLException
    {
        Connection connection = getDataSource().getConnection();
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
        Connection connection = getDataSource().getConnection( username, password );
        return monitor( connection );
    }

    public int getLoginTimeout()
        throws SQLException
    {
        return getDataSource().getLoginTimeout();
    }

    public PrintWriter getLogWriter()
        throws SQLException
    {
        return getDataSource().getLogWriter();
    }

    public void setLoginTimeout( int seconds )
        throws SQLException
    {
        getDataSource().setLoginTimeout( seconds );
    }

    public void setLogWriter( PrintWriter out )
        throws SQLException
    {
        getDataSource().setLogWriter( out );
    }

    // --- jdbc4 ----

    public boolean isWrapperFor( Class<?> iface )
        throws SQLException
    {
        return getDataSource().isWrapperFor( iface );
    }

    public <T> T unwrap( Class<T> iface )
        throws SQLException
    {
        return getDataSource().unwrap( iface );
    }

}
