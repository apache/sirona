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

package org.apache.commons.monitoring.spring.config;

import javax.servlet.ServletContext;

import org.apache.commons.monitoring.instrumentation.servlet.ServletContextUtil;
import org.springframework.web.context.ServletContextAware;

/**
 * Creates monitored proxies for beans that match a pointcut.
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class WebappRepositoryFactoryBean extends RepositoryFactoryBean
    implements ServletContextAware
{
    /** The web application context */
    private ServletContext servletContext;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext( ServletContext servletContext )
    {
        this.servletContext = servletContext;
    };

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
        throws Exception
    {
        super.afterPropertiesSet();
        if ( servletContext != null )
        {
            servletContext.setAttribute( ServletContextUtil.REPOSITORY_KEY, getObject() );
        }
    }

}
