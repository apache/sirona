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
package org.apache.test.sirona.javaagent;

import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JavaAgentRunner.class)
public class DebugTest {
    @Test
    public void debug() throws Throwable {
        new ServiceNoStaticTransform().f();
    }

    public static class ServiceNoStaticTransform {
        public void f() {
            try {
                throw new NullPointerException();
            } catch (final NullPointerException iae) {
                // no-op
            }
        }

        public void f2() {
            AgentContext localAgentContext = AgentContext.startOn(this, "org.apache.test.sirona.javaagent.DebugTest$ServiceNoStaticTransform.f");
            try {
                try {
                    throw new NullPointerException();
                } catch (NullPointerException iae) {
                    localAgentContext.stop(null);
                    return;
                }
            } catch (Throwable localThrowable) {
                localAgentContext.stopWithException(localThrowable);
                throw new RuntimeException(localThrowable);
            }
        }
    }
}
