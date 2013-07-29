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

import org.aopalliance.aop.Advice;
import org.apache.commons.monitoring.instrumentation.aop.MonitorNameExtractor;
import org.apache.commons.monitoring.spring.MonitoringAdviceFactory.MonitoringConfigSource;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;

/**
 * Creates monitored proxies for beans that match a naming pattern.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class BeanNameMonitoringAutoProxyCreator
    extends BeanNameAutoProxyCreator implements MonitoringConfigSource {
    private String category;

    private MonitorNameExtractor monitorNameExtractor;

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class,
     * java.lang.String, org.springframework.aop.TargetSource)
     */
    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName, TargetSource targetSource) {
        if (super.getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource) != DO_NOT_PROXY) {
            Advice advice = MonitoringAdviceFactory.getAdvice(this);
            return new Object[]{advice};
        }
        return DO_NOT_PROXY;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @param monitorNameExtractor the monitorNameExtractor to set
     */
    public void setMonitorNameExtractor(MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }

    public String getCategory() {
        return category;
    }

    public MonitorNameExtractor getMonitorNameExtractor() {
        return monitorNameExtractor;
    }
}
