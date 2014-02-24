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

package org.apache.sirona.spring;

import org.aopalliance.aop.Advice;
import org.apache.sirona.aop.DefaultMonitorNameExtractor;
import org.apache.sirona.aop.MonitorNameExtractor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates monitored proxies for beans that match a pointcut.
 *
 *
 */
public class PointcutMonitoringAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
    private MonitorNameExtractor monitorNameExtractor = DefaultMonitorNameExtractor.INSTANCE;
    private Pointcut pointcut;

    @Override
    protected List<Advisor> findCandidateAdvisors() {
        final AopaliancePerformanceInterceptor interceptor = new AopaliancePerformanceInterceptor();
        interceptor.setMonitorNameExtractor(monitorNameExtractor);

        final PointcutAdvisor adivsor = createPointcutAdvisor(interceptor);

        final List<Advisor> adivisors = new ArrayList<Advisor>(1);
        adivisors.add(adivsor);
        return adivisors;
    }

    protected PointcutAdvisor createPointcutAdvisor(Advice advice) {
        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    public void setMonitorNameExtractor(final MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }

    public void setPointcut(final Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    public MonitorNameExtractor getMonitorNameExtractor() {
        return monitorNameExtractor;
    }
}
