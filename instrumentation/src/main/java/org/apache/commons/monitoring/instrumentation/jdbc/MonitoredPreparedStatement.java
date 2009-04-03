package org.apache.commons.monitoring.instrumentation.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;

/**
 * @author ndeloof
 *
 */
public class MonitoredPreparedStatement
    extends MonitoredStatement
    implements PreparedStatement
{
    private PreparedStatement statement;

    protected String sql;

    /**
     * @param statement
     * @param repository
     */
    public MonitoredPreparedStatement( PreparedStatement statement, String sql, Repository repository )
    {
        super( statement, repository );
        this.statement = statement;
        this.sql = sql;
    }

    // --- delegate methods ---

    public final void addBatch()
        throws SQLException
    {
        statement.addBatch();
    }

    public final void clearParameters()
        throws SQLException
    {
        statement.clearParameters();
    }

    public final boolean execute()
        throws SQLException
    {
        StopWatch stopWatch = repository.start( repository.getMonitor( sql, "jdbc" ) );
        try
        {
            return statement.execute();
        }
        catch ( SQLException sqle )
        {
            throw monitor( sqle );
        }
        finally
        {
            stopWatch.stop();
        }
    }

    public final ResultSet executeQuery()
        throws SQLException
    {
        StopWatch stopWatch = repository.start( repository.getMonitor( sql, "jdbc" ) );
        try
        {
            return statement.executeQuery();
        }
        catch ( SQLException sqle )
        {
            throw monitor( sqle );
        }
        finally
        {
            stopWatch.stop();
        }
    }

    public final int executeUpdate()
        throws SQLException
    {
        StopWatch stopWatch = repository.start( repository.getMonitor( sql, "jdbc" ) );
        try
        {
            return statement.executeUpdate();
        }
        catch ( SQLException sqle )
        {
            throw monitor( sqle );
        }
        finally
        {
            stopWatch.stop();
        }
    }

    public final ResultSetMetaData getMetaData()
        throws SQLException
    {
        return statement.getMetaData();
    }

    public final ParameterMetaData getParameterMetaData()
        throws SQLException
    {
        return statement.getParameterMetaData();
    }

    public final void setArray( int i, Array x )
        throws SQLException
    {
        statement.setArray( i, x );
    }

    public final void setAsciiStream( int parameterIndex, InputStream x, int length )
        throws SQLException
    {
        statement.setAsciiStream( parameterIndex, x, length );
    }

    public final void setBigDecimal( int parameterIndex, BigDecimal x )
        throws SQLException
    {
        statement.setBigDecimal( parameterIndex, x );
    }

    public final void setBinaryStream( int parameterIndex, InputStream x, int length )
        throws SQLException
    {
        statement.setBinaryStream( parameterIndex, x, length );
    }

    public final void setBlob( int i, Blob x )
        throws SQLException
    {
        statement.setBlob( i, x );
    }

    public final void setBoolean( int parameterIndex, boolean x )
        throws SQLException
    {
        statement.setBoolean( parameterIndex, x );
    }

    public final void setByte( int parameterIndex, byte x )
        throws SQLException
    {
        statement.setByte( parameterIndex, x );
    }

    public final void setBytes( int parameterIndex, byte[] x )
        throws SQLException
    {
        statement.setBytes( parameterIndex, x );
    }

    public final void setCharacterStream( int parameterIndex, Reader reader, int length )
        throws SQLException
    {
        statement.setCharacterStream( parameterIndex, reader, length );
    }

    public final void setClob( int i, Clob x )
        throws SQLException
    {
        statement.setClob( i, x );
    }

    public final void setDate( int parameterIndex, Date x, Calendar cal )
        throws SQLException
    {
        statement.setDate( parameterIndex, x, cal );
    }

    public final void setDate( int parameterIndex, Date x )
        throws SQLException
    {
        statement.setDate( parameterIndex, x );
    }

    public final void setDouble( int parameterIndex, double x )
        throws SQLException
    {
        statement.setDouble( parameterIndex, x );
    }

    public final void setFloat( int parameterIndex, float x )
        throws SQLException
    {
        statement.setFloat( parameterIndex, x );
    }

    public final void setInt( int parameterIndex, int x )
        throws SQLException
    {
        statement.setInt( parameterIndex, x );
    }

    public final void setLong( int parameterIndex, long x )
        throws SQLException
    {
        statement.setLong( parameterIndex, x );
    }

    public final void setNull( int paramIndex, int sqlType, String typeName )
        throws SQLException
    {
        statement.setNull( paramIndex, sqlType, typeName );
    }

    public final void setNull( int parameterIndex, int sqlType )
        throws SQLException
    {
        statement.setNull( parameterIndex, sqlType );
    }

    public final void setObject( int parameterIndex, Object x, int targetSqlType, int scale )
        throws SQLException
    {
        statement.setObject( parameterIndex, x, targetSqlType, scale );
    }

    public final void setObject( int parameterIndex, Object x, int targetSqlType )
        throws SQLException
    {
        statement.setObject( parameterIndex, x, targetSqlType );
    }

    public final void setObject( int parameterIndex, Object x )
        throws SQLException
    {
        statement.setObject( parameterIndex, x );
    }

    public final void setRef( int i, Ref x )
        throws SQLException
    {
        statement.setRef( i, x );
    }

    public final void setShort( int parameterIndex, short x )
        throws SQLException
    {
        statement.setShort( parameterIndex, x );
    }

    public final void setString( int parameterIndex, String x )
        throws SQLException
    {
        statement.setString( parameterIndex, x );
    }

    public final void setTime( int parameterIndex, Time x, Calendar cal )
        throws SQLException
    {
        statement.setTime( parameterIndex, x, cal );
    }

    public final void setTime( int parameterIndex, Time x )
        throws SQLException
    {
        statement.setTime( parameterIndex, x );
    }

    public final void setTimestamp( int parameterIndex, Timestamp x, Calendar cal )
        throws SQLException
    {
        statement.setTimestamp( parameterIndex, x, cal );
    }

    public final void setTimestamp( int parameterIndex, Timestamp x )
        throws SQLException
    {
        statement.setTimestamp( parameterIndex, x );
    }

    public final void setUnicodeStream( int parameterIndex, InputStream x, int length )
        throws SQLException
    {
        statement.setUnicodeStream( parameterIndex, x, length );
    }

    public final void setURL( int parameterIndex, URL x )
        throws SQLException
    {
        statement.setURL( parameterIndex, x );
    }

}
