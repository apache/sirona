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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.apache.commons.monitoring.Repository;

/**
 * @author ndeloof
 *
 */
public class MonitoredConnectionHandler
    implements InvocationHandler
{
    /** target connection */
    private Connection connection;

    private Repository repository;

    private ConnectionClosedCallBack callBack;

    public MonitoredConnectionHandler( Connection connection, Repository repository, ConnectionClosedCallBack callBack )
    {
        super();
        this.connection = connection;
        this.repository = repository;
        this.callBack = callBack;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        String name = method.getName();
        if ( name.equals( "createStatement" ) )
        {
            Statement statement = (Statement) method.invoke( connection, args );
            return Proxy.newProxyInstance( getClassLoader(), new Class[] { Statement.class },
                new MonitoredStatementHandler( repository, statement ) );
        }
        else if ( name.equals( "prepareStatement" ) )
        {
            PreparedStatement statement = (PreparedStatement) method.invoke( connection, args );
            return Proxy.newProxyInstance( getClassLoader(), new Class[] { PreparedStatement.class },
                new MonitoredStatementHandler( repository, statement ) );
        }
        else if ( name.equals( "prepareCall" ) )
        {
            CallableStatement statement = (CallableStatement) method.invoke( connection, args );
            return Proxy.newProxyInstance( getClassLoader(), new Class[] { CallableStatement.class },
                new MonitoredStatementHandler( repository, statement ) );
        }
        else if ( name.equals( "close" ) )
        {
            callBack.onConnectionClosed();
        }
        return method.invoke( connection, args );
    }

    /**
     * @return
     */
    private ClassLoader getClassLoader()
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl == null )
        {
            cl = getClass().getClassLoader();
        }
        return cl;
    }
}
