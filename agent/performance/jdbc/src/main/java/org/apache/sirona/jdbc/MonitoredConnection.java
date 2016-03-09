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

import org.apache.sirona.counters.Counter;
import org.apache.sirona.stopwatches.CounterStopWatch;
import org.apache.sirona.stopwatches.StopWatch;
import org.apache.sirona.util.ClassLoaders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 *
 */
public class MonitoredConnection implements InvocationHandler {
    private Connection connection;
    private StopWatch stopWatch;

    /**
     * @param connection target connection
     */
    public MonitoredConnection(final Connection connection, final StopWatch stopWatch) {
        this.connection = connection;
        this.stopWatch = stopWatch;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String name = method.getName();
        if ("close".equals(name)) {
            connection.close();
            stopWatch.stop();
            return null;
        }

        if (name.startsWith("prepare") || name.startsWith("create")) {
            final Class<?> returnType = method.getReturnType();
            if (CallableStatement.class.equals(returnType)) {
                return monitor(CallableStatement.class.cast(doInvoke(method, args)), (String) args[0]);
            } else if (PreparedStatement.class.equals(returnType)) {
                return monitor(PreparedStatement.class.cast(doInvoke(method, args)), (String) args[0]);
            } else if (Statement.class.equals(returnType)) {
                return monitor(Statement.class.cast(doInvoke(method, args)));
            }
        }

        return doInvoke(method, args);
    }

    private Object doInvoke(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(connection, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    private Statement monitor(final Statement statement) {
        return Statement.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{Statement.class}, new MonitoredStatement(statement)));
    }

    /**
     * @param statement traget PreparedStatement
     * @param sql       SQL Query
     * @return monitored PreparedStatement
     */
    private PreparedStatement monitor(final PreparedStatement statement, final String sql) {
        return PreparedStatement.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{PreparedStatement.class}, new MonitoredPreparedStatement(statement, sql)));
    }

    /**
     * @param statement target PreparedStatement
     * @param sql       SQL Query
     * @return Monitored CallableStatement
     */
    private CallableStatement monitor(final CallableStatement statement, final String sql) {
        return CallableStatement.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{CallableStatement.class}, new MonitoredPreparedStatement(statement, sql)));
    }

    public static Connection monitor(final Connection connection, final Counter counter) {
        final StopWatch stopWatch = new CounterStopWatch(counter);
        return Connection.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{Connection.class}, new MonitoredConnection(connection, stopWatch)));
    }
}
