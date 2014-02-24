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

package org.apache.sirona.jdbc;


import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;


/**
 *
 */
public class MonitoredDataSource implements DataSource {
    /**
     * delegate DataSource
     */
    private DataSource dataSource;

    /**
     * dataSource name
     */
    private String dataSourceName = DataSource.class.getName();
    private Counter counter;

    /**
     * Constructor
     *
     * @param dataSource the datasource to counter
     */
    public MonitoredDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, dataSourceName));
    }

    public MonitoredDataSource() {
        super();
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName(final String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(final Counter counter) {
        this.counter = counter;
    }

    protected Connection monitor(final Connection connection) {
        return MonitoredConnection.monitor(connection, counter);
    }

    /**
     * @return the dataSource
     */
    protected DataSource getDataSource() {
        return dataSource;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection()
        throws SQLException {
        return monitor(getDataSource().getConnection());
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password)
        throws SQLException {
        return monitor(getDataSource().getConnection(username, password));
    }

    public int getLoginTimeout()
        throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    public PrintWriter getLogWriter()
        throws SQLException {
        return getDataSource().getLogWriter();
    }

    public void setLoginTimeout(int seconds)
        throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out)
        throws SQLException {
        getDataSource().setLogWriter(out);
    }

    // --- jdbc4 ----

    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface)
        throws SQLException {
        return getDataSource().unwrap(iface);
    }

    public Logger getParentLogger()
        throws SQLFeatureNotSupportedException {
        return Logger.getLogger("org.apache.sirona.jdbc.datasource");
    }
}
