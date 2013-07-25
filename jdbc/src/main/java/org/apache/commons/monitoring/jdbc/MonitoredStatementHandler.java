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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;

/**
 * @author ndeloof
 *
 */
public class MonitoredStatementHandler
    implements InvocationHandler
{

    private Repository repository;

    private Statement statement;

    /**
     * @param repository
     * @param statement
     */
    public MonitoredStatementHandler( Repository repository, Statement statement )
    {
        super();
        this.repository = repository;
        this.statement = statement;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if ( method.getName().startsWith( "execute" ) )
        {
            // skip executeUpdate
            String sql = ( args.length > 0 ? (String) args[0] : "batch" );
            StopWatch stopWatch = repository.start( repository.getMonitor( sql, "jdbc" ) );
            try
            {
                return method.invoke( statement, args );
            }
            catch ( InvocationTargetException ite )
            {
                if ( ite.getCause() instanceof SQLException )
                {
                    SQLException sqle = (SQLException) ite.getCause();
                    String name = "SQLException:" + sqle.getSQLState() + ":" + sqle.getErrorCode();
                    Monitor monitor = repository.getMonitor( name, "jdbc" );
                    monitor.getCounter( Monitor.FAILURES ).add( 1 );
                    throw sqle;
                }
                throw ite;
            }
            finally
            {
                stopWatch.stop();
            }
        }
        return method.invoke( proxy, args );
    }
}
