package org.apache.commons.monitoring.instrumentation.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.monitoring.Repository;

/**
 * @author ndeloof
 *
 */
public class MonitoredCallableStatement
    extends MonitoredPreparedStatement
    implements CallableStatement
{
    private CallableStatement statement;

    /**
     * @param statement
     * @param sql
     * @param repository
     */
    public MonitoredCallableStatement( CallableStatement statement, String sql, Repository repository )
    {
        super( statement, sql, repository );
        this.statement = statement;
    }

    // --- delegate methods ---

    public Array getArray( int i )
        throws SQLException
    {
        return statement.getArray( i );
    }

    public Array getArray( String parameterName )
        throws SQLException
    {
        return statement.getArray( parameterName );
    }

    @Deprecated
    public BigDecimal getBigDecimal( int parameterIndex, int scale )
        throws SQLException
    {
        return statement.getBigDecimal( parameterIndex, scale );
    }

    public BigDecimal getBigDecimal( int parameterIndex )
        throws SQLException
    {
        return statement.getBigDecimal( parameterIndex );
    }

    public BigDecimal getBigDecimal( String parameterName )
        throws SQLException
    {
        return statement.getBigDecimal( parameterName );
    }

    public Blob getBlob( int i )
        throws SQLException
    {
        return statement.getBlob( i );
    }

    public Blob getBlob( String parameterName )
        throws SQLException
    {
        return statement.getBlob( parameterName );
    }

    public boolean getBoolean( int parameterIndex )
        throws SQLException
    {
        return statement.getBoolean( parameterIndex );
    }

    public boolean getBoolean( String parameterName )
        throws SQLException
    {
        return statement.getBoolean( parameterName );
    }

    public byte getByte( int parameterIndex )
        throws SQLException
    {
        return statement.getByte( parameterIndex );
    }

    public byte getByte( String parameterName )
        throws SQLException
    {
        return statement.getByte( parameterName );
    }

    public byte[] getBytes( int parameterIndex )
        throws SQLException
    {
        return statement.getBytes( parameterIndex );
    }

    public byte[] getBytes( String parameterName )
        throws SQLException
    {
        return statement.getBytes( parameterName );
    }

    public Clob getClob( int i )
        throws SQLException
    {
        return statement.getClob( i );
    }

    public Clob getClob( String parameterName )
        throws SQLException
    {
        return statement.getClob( parameterName );
    }

    public Date getDate( int parameterIndex, Calendar cal )
        throws SQLException
    {
        return statement.getDate( parameterIndex, cal );
    }

    public Date getDate( int parameterIndex )
        throws SQLException
    {
        return statement.getDate( parameterIndex );
    }

    public Date getDate( String parameterName, Calendar cal )
        throws SQLException
    {
        return statement.getDate( parameterName, cal );
    }

    public Date getDate( String parameterName )
        throws SQLException
    {
        return statement.getDate( parameterName );
    }

    public double getDouble( int parameterIndex )
        throws SQLException
    {
        return statement.getDouble( parameterIndex );
    }

    public double getDouble( String parameterName )
        throws SQLException
    {
        return statement.getDouble( parameterName );
    }

    public float getFloat( int parameterIndex )
        throws SQLException
    {
        return statement.getFloat( parameterIndex );
    }

    public float getFloat( String parameterName )
        throws SQLException
    {
        return statement.getFloat( parameterName );
    }

    public int getInt( int parameterIndex )
        throws SQLException
    {
        return statement.getInt( parameterIndex );
    }

    public int getInt( String parameterName )
        throws SQLException
    {
        return statement.getInt( parameterName );
    }

    public long getLong( int parameterIndex )
        throws SQLException
    {
        return statement.getLong( parameterIndex );
    }

    public long getLong( String parameterName )
        throws SQLException
    {
        return statement.getLong( parameterName );
    }

    public Object getObject( int i, Map<String, Class<?>> map )
        throws SQLException
    {
        return statement.getObject( i, map );
    }

    public Object getObject( int parameterIndex )
        throws SQLException
    {
        return statement.getObject( parameterIndex );
    }

    public Object getObject( String parameterName, Map<String, Class<?>> map )
        throws SQLException
    {
        return statement.getObject( parameterName, map );
    }

    public Object getObject( String parameterName )
        throws SQLException
    {
        return statement.getObject( parameterName );
    }

    public Ref getRef( int i )
        throws SQLException
    {
        return statement.getRef( i );
    }

    public Ref getRef( String parameterName )
        throws SQLException
    {
        return statement.getRef( parameterName );
    }

    public short getShort( int parameterIndex )
        throws SQLException
    {
        return statement.getShort( parameterIndex );
    }

    public short getShort( String parameterName )
        throws SQLException
    {
        return statement.getShort( parameterName );
    }

    public String getString( int parameterIndex )
        throws SQLException
    {
        return statement.getString( parameterIndex );
    }

    public String getString( String parameterName )
        throws SQLException
    {
        return statement.getString( parameterName );
    }

    public Time getTime( int parameterIndex, Calendar cal )
        throws SQLException
    {
        return statement.getTime( parameterIndex, cal );
    }

    public Time getTime( int parameterIndex )
        throws SQLException
    {
        return statement.getTime( parameterIndex );
    }

    public Time getTime( String parameterName, Calendar cal )
        throws SQLException
    {
        return statement.getTime( parameterName, cal );
    }

    public Time getTime( String parameterName )
        throws SQLException
    {
        return statement.getTime( parameterName );
    }

    public Timestamp getTimestamp( int parameterIndex, Calendar cal )
        throws SQLException
    {
        return statement.getTimestamp( parameterIndex, cal );
    }

    public Timestamp getTimestamp( int parameterIndex )
        throws SQLException
    {
        return statement.getTimestamp( parameterIndex );
    }

    public Timestamp getTimestamp( String parameterName, Calendar cal )
        throws SQLException
    {
        return statement.getTimestamp( parameterName, cal );
    }

    public Timestamp getTimestamp( String parameterName )
        throws SQLException
    {
        return statement.getTimestamp( parameterName );
    }

    public URL getURL( int parameterIndex )
        throws SQLException
    {
        return statement.getURL( parameterIndex );
    }

    public URL getURL( String parameterName )
        throws SQLException
    {
        return statement.getURL( parameterName );
    }

    public void registerOutParameter( int parameterIndex, int sqlType, int scale )
        throws SQLException
    {
        statement.registerOutParameter( parameterIndex, sqlType, scale );
    }

    public void registerOutParameter( int paramIndex, int sqlType, String typeName )
        throws SQLException
    {
        statement.registerOutParameter( paramIndex, sqlType, typeName );
    }

    public void registerOutParameter( int parameterIndex, int sqlType )
        throws SQLException
    {
        statement.registerOutParameter( parameterIndex, sqlType );
    }

    public void registerOutParameter( String parameterName, int sqlType, int scale )
        throws SQLException
    {
        statement.registerOutParameter( parameterName, sqlType, scale );
    }

    public void registerOutParameter( String parameterName, int sqlType, String typeName )
        throws SQLException
    {
        statement.registerOutParameter( parameterName, sqlType, typeName );
    }

    public void registerOutParameter( String parameterName, int sqlType )
        throws SQLException
    {
        statement.registerOutParameter( parameterName, sqlType );
    }

    public void setAsciiStream( String parameterName, InputStream x, int length )
        throws SQLException
    {
        statement.setAsciiStream( parameterName, x, length );
    }

    public void setBigDecimal( String parameterName, BigDecimal x )
        throws SQLException
    {
        statement.setBigDecimal( parameterName, x );
    }

    public void setBinaryStream( String parameterName, InputStream x, int length )
        throws SQLException
    {
        statement.setBinaryStream( parameterName, x, length );
    }

    public void setBoolean( String parameterName, boolean x )
        throws SQLException
    {
        statement.setBoolean( parameterName, x );
    }

    public void setByte( String parameterName, byte x )
        throws SQLException
    {
        statement.setByte( parameterName, x );
    }

    public void setBytes( String parameterName, byte[] x )
        throws SQLException
    {
        statement.setBytes( parameterName, x );
    }

    public void setCharacterStream( String parameterName, Reader reader, int length )
        throws SQLException
    {
        statement.setCharacterStream( parameterName, reader, length );
    }

    public void setDate( String parameterName, Date x, Calendar cal )
        throws SQLException
    {
        statement.setDate( parameterName, x, cal );
    }

    public void setDate( String parameterName, Date x )
        throws SQLException
    {
        statement.setDate( parameterName, x );
    }

    public void setDouble( String parameterName, double x )
        throws SQLException
    {
        statement.setDouble( parameterName, x );
    }

    public void setFloat( String parameterName, float x )
        throws SQLException
    {
        statement.setFloat( parameterName, x );
    }

    public void setInt( String parameterName, int x )
        throws SQLException
    {
        statement.setInt( parameterName, x );
    }

    public void setLong( String parameterName, long x )
        throws SQLException
    {
        statement.setLong( parameterName, x );
    }

    public void setNull( String parameterName, int sqlType, String typeName )
        throws SQLException
    {
        statement.setNull( parameterName, sqlType, typeName );
    }

    public void setNull( String parameterName, int sqlType )
        throws SQLException
    {
        statement.setNull( parameterName, sqlType );
    }

    public void setObject( String parameterName, Object x, int targetSqlType, int scale )
        throws SQLException
    {
        statement.setObject( parameterName, x, targetSqlType, scale );
    }

    public void setObject( String parameterName, Object x, int targetSqlType )
        throws SQLException
    {
        statement.setObject( parameterName, x, targetSqlType );
    }

    public void setObject( String parameterName, Object x )
        throws SQLException
    {
        statement.setObject( parameterName, x );
    }

    public void setShort( String parameterName, short x )
        throws SQLException
    {
        statement.setShort( parameterName, x );
    }

    public void setString( String parameterName, String x )
        throws SQLException
    {
        statement.setString( parameterName, x );
    }

    public void setTime( String parameterName, Time x, Calendar cal )
        throws SQLException
    {
        statement.setTime( parameterName, x, cal );
    }

    public void setTime( String parameterName, Time x )
        throws SQLException
    {
        statement.setTime( parameterName, x );
    }

    public void setTimestamp( String parameterName, Timestamp x, Calendar cal )
        throws SQLException
    {
        statement.setTimestamp( parameterName, x, cal );
    }

    public void setTimestamp( String parameterName, Timestamp x )
        throws SQLException
    {
        statement.setTimestamp( parameterName, x );
    }

    public void setURL( String parameterName, URL val )
        throws SQLException
    {
        statement.setURL( parameterName, val );
    }

    public boolean wasNull()
        throws SQLException
    {
        return statement.wasNull();
    }

}
