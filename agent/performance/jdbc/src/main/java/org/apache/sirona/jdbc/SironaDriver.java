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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class SironaDriver implements Driver {
    static {
        try {
            DriverManager.registerDriver(new SironaDriver());
        } catch (final SQLException e) {
            // no-op
        }
    }

    private static final String PREFIX = "jdbc:sirona:";
    private static final String DRIVER_SUFFIX = "delegateDriver=";

    public static void load() {
    } // sexier than Class.forName("org.apache.sirona.jdbc.SironaDriver"); in full java

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException("Driver " + SironaDriver.class.getName() + " doesn't accept " + url + ". Pattern is jdbc:sirona:<xxx>:<yyy>?delegateDriver=<zzz>");
        }

        final int driverIndex = url.indexOf(DRIVER_SUFFIX);

        String realUrl = "jdbc:" + url.substring(PREFIX.length(), driverIndex);
        if (realUrl.endsWith("?") || realUrl.endsWith("&")) {
            realUrl = realUrl.substring(0, realUrl.length() - 1);
        }

        final String realDriver = url.substring(driverIndex + DRIVER_SUFFIX.length());
        try {
            final Driver delegate = Driver.class.cast(Class.forName(realDriver).newInstance());
            return MonitoredConnection.monitor(delegate.connect(realUrl, info), Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, url)));
        } catch (final Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean acceptsURL(final String url) throws SQLException {
        return url != null && url.startsWith(PREFIX) && url.contains(DRIVER_SUFFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    // @Override // java 7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger("org.apache.sirona.jdbc.driver");
    }
}
