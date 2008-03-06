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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.monitoring.Monitoring;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.impl.repositories.DefaultRepository;

/**
 * A Servlet Context Listener to configure Monitoring
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringListener
    implements ServletContextListener
{

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized( ServletContextEvent event )
    {
        Repository repository;
        String name = event.getServletContext().getInitParameter( ServletContextUtil.REPOSITORY_KEY );
        if ( name != null )
        {
            try
            {
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
        event.getServletContext().setAttribute( ServletContextUtil.REPOSITORY_KEY, repository );
        Monitoring.setRepository( repository );
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
