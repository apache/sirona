package org.apache.commons.monitoring.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Metric.Type;
import org.apache.commons.monitoring.stopwatches.DefaultStopWatch;

/**
 * @author ndeloof
 *
 */
public class AbstractMonitoredDataSource
{
    private final static Role OPEN_CONECTIONS =
        new Role( "open connections", Unit.UNARY, Type.GAUGE );

    private final static Role CONECTION_DURATION =
        new Role( "connection duration", Unit.Time.NANOSECOND, Type.COUNTER );

    /** delegate DataSource */
    private DataSource dataSource;

    /** dataSource name */
    private String dataSourceName = DataSource.class.getName();

    private Repository repository;

    private Monitor monitor;

    /**
     * Constructor
     * <p>
     * Used to configure a dataSource via setter-based dependency injection
     */
    public AbstractMonitoredDataSource()
    {
        super();
    }

    /**
     * Constructor
     * 
     * @param dataSource the datasource to monitor
     * @param repository the Monitoring repository
     */
    public AbstractMonitoredDataSource( DataSource dataSource, Repository repository )
    {
        this.setDataSource( dataSource );
        this.repository = repository;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName( String dataSourceName )
    {
        this.dataSourceName = dataSourceName;
    }

    /**
     * required
     *
     * @param repository
     */
    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }

    public void init()
    {
        if ( monitor == null )
        {
            monitor = repository.getMonitor( dataSourceName, "jdbc" );
        }
    }

    protected Connection monitor( Connection connection )
    {
        // Computes the number of open connections and the connection duration
        final StopWatch stopWatch = new DefaultStopWatch( monitor, OPEN_CONECTIONS, CONECTION_DURATION );
        return new MonitoredConnection( connection, repository, new ConnectionClosedCallBack()
        {
            public void onConnectionClosed()
            {
                stopWatch.stop();
            }
        } );
    }

    /**
     * @return the dataSource
     */
    protected DataSource getDataSource()
    {
        return dataSource;
    }

}