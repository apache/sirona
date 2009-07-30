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
