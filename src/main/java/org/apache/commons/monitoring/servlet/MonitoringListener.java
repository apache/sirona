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

package org.apache.commons.monitoring.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Monitoring;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.repositories.DefaultRepository;
import org.apache.commons.monitoring.listeners.HistorizedRepositoryDecorator;

/**
 * A Servlet Context Listener to configure Monitoring, used to setup optional
 * monitoring features
 * <p>
 * To enable repository historization, set the servlet context parameter
 * <tt>org.apache.commons.monitoring.History</tt> using the format :
 * <tt>[historization period][unit]:[size]</tt>
 * <p>
 * for example, to setup a 30 items history of 10 minutes periods  :
 * <pre>
 *  &lt;context-param&gt;
 *  &lt;description&gt;enable monitoring history&lt;/description&gt;
 *  &lt;param-name&gt;org.apache.commons.monitoring.History&lt;/param-name&gt;
 *  &lt;param-value&gt;10min:30&lt;/param-value&gt;
 *  &lt;/context-param&gt;
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringListener
    implements ServletContextListener
{

    public static final String HISTORY = "org.apache.commons.monitoring.History";

    public static final String HISTORY_PERIOD = "org.apache.commons.monitoring.History.period";

    public static final String HISTORY_SIZE = "org.apache.commons.monitoring.History.size";

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized( ServletContextEvent event )
    {
        ServletContext sc = event.getServletContext();
        Repository repository = createRepository( sc );

        String history = sc.getInitParameter( HISTORY );
        if ( history != null )
        {
            repository = setupHistory( repository, history );
        }

        sc.setAttribute( ServletContextUtil.REPOSITORY_KEY, repository );
        Monitoring.setRepository( repository );
    }

    /**
     * Create the repository to be used in this web application.
     * <p>
     * User can override this method to change the repository instanciation strategy and/or to
     * supply a custom implementation.
     * @param sc
     * @return
     */
    protected Repository createRepository( ServletContext sc )
    {
        Repository repository;
        String name = sc.getInitParameter( ServletContextUtil.REPOSITORY_KEY );
        if ( name != null )
        {
            try
            {
                @SuppressWarnings( "unchecked" )
                Class repositoryClass = Class.forName( name );
                if ( !Repository.class.isAssignableFrom( repositoryClass ) )
                {
                    throw new IllegalStateException( "The class " + name + " does not implements "
                        + Repository.class.getName() );
                }
                repository = (Repository) repositoryClass.newInstance();
            }
            catch ( ClassNotFoundException e )
            {
                throw new IllegalStateException( "The repository class name " + name + " is not a valid class" );
            }
            catch ( InstantiationException e )
            {
                throw new IllegalStateException( "Failed to create repository instance from class " + name, e );
            }
            catch ( IllegalAccessException e )
            {
                throw new IllegalStateException( "Failed to create repository instance from class " + name, e );
            }
        }
        else
        {
            repository = new DefaultRepository();
        }
        return repository;
    }

    /**
     * Setup the historization feature on the repository
     *
     * @param repository the (observable) repository to historize
     * @param history the historization configuration, in format
     * [period][unit]:[size]
     * @return
     */
    protected Repository setupHistory( Repository repository, String history )
    {
        Repository.Observable observable = (Repository.Observable) repository;
        int idx = history.indexOf( ':' );
        char[] ch = history.toCharArray();
        long period = 0;
        for ( int i = 0; i < idx; i++ )
        {
            if ( Character.isDigit( ch[i] ) )
                continue;
            period = Long.parseLong( history.substring( 0, i ) );
            String unit = history.substring( i, idx );
            if ( unit != null )
            {
                Unit u = Unit.NANOS.getDerived( unit );
                period *= u.getScale() / Unit.MILLIS.getScale();
                break;
            }
        }

        int size = Integer.parseInt( history.substring( idx + 1 ) );
        repository = new HistorizedRepositoryDecorator( period, size, observable );
        return repository;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed( ServletContextEvent event )
    {
        // TODO Auto-generated method stub

    }
}
