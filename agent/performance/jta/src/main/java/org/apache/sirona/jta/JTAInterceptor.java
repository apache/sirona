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
package org.apache.sirona.jta;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

@Interceptor
@JTAMonitored
public class JTAInterceptor {
    private static final String RESOURCE_KEY = JTAInterceptor.class.getName();

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @AroundInvoke
    @AroundTimeout
    public Object jta(final InvocationContext invocationContext) throws Exception {
        if (transactionSynchronizationRegistry.getTransactionStatus() == Status.STATUS_ACTIVE
                && transactionSynchronizationRegistry.getResource(RESOURCE_KEY) == null) {
            JTAGauges.ACTIVE.incrementAndGet();
            transactionSynchronizationRegistry.putResource(RESOURCE_KEY, Boolean.TRUE);
            transactionSynchronizationRegistry.registerInterposedSynchronization(new JTACounterSynchronization());
        }
        return invocationContext.proceed();
    }

    private static class JTACounterSynchronization implements Synchronization {
        @Override
        public void beforeCompletion() {
            // no-op
        }

        @Override
        public void afterCompletion(final int status) {
            if (status == Status.STATUS_COMMITTED) {
                JTAGauges.COMMITTED.incrementAndGet();
            } else if (status == Status.STATUS_ROLLEDBACK) {
                JTAGauges.ROLLBACKED.incrementAndGet();
            }
            JTAGauges.ACTIVE.decrementAndGet();
        }
    }
}
