package org.apache.commons.monitoring.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.jdbc.MonitoredConnection;
import org.apache.commons.monitoring.jdbc.MonitoredConnection.ConnectionClosedCallBack;

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
