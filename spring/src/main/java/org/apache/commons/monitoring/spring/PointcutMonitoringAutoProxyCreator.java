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

package org.apache.commons.monitoring.spring;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.monitoring.instrumentation.aop.MonitorNameExtractor;
import org.apache.commons.monitoring.spring.MonitoringAdviceFactory.MonitoringConfigSource;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * Creates monitored proxies for beans that match a pointcut.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class PointcutMonitoringAutoProxyCreator
    extends AbstractAdvisorAutoProxyCreator
    implements MonitoringConfigSource
{
    private String category;

    private String subsystem;

    private MonitorNameExtractor monitorNameExtractor;

    private Pointcut pointcut;

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
     */
    @Override
    protected List<Advisor> findCandidateAdvisors()
    {
        List<Advisor> adivisors = new ArrayList<Advisor>( 1 );

        PointcutAdvisor adivsor = createPointcutAdvisor( MonitoringAdviceFactory.getAdvice( this ) );

        adivisors.add( adivsor );
        return adivisors;
    }

    /**
     * @param interceptor
     * @return
     */
    protected PointcutAdvisor createPointcutAdvisor( Advice advice )
    {
        return new DefaultPointcutAdvisor( pointcut, advice );
    }

    /**
     * @param category the category to set
     */
    public void setCategory( String category )
    {
        this.category = category;
    }

    /**
     * @param subsystem the subsystem to set
     */
    public void setSubsystem( String subsystem )
    {
        this.subsystem = subsystem;
    }

    /**
     * @param monitorNameExtractor the monitorNameExtractor to set
     */
    public void setMonitorNameExtractor( MonitorNameExtractor monitorNameExtractor )
    {
        this.monitorNameExtractor = monitorNameExtractor;
    }

    /**
     * @param pointcut the pointcut to set
     */
    public void setPointcut( Pointcut pointcut )
    {
        this.pointcut = pointcut;
    }

    /**
     * @return the category
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * @return the subsystem
     */
    public String getSubsystem()
    {
        return subsystem;
    }

    /**
     * @return the monitorNameExtractor
     */
    public MonitorNameExtractor getMonitorNameExtractor()
    {
        return monitorNameExtractor;
    }
}
