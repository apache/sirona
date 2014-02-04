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

import org.apache.sirona.javaagent.listener.ConfigurableListener;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultExclusionTest {
    @Test
    public void business() {
        assertFalse(ConfigurableListener.DEFAULT_EXCLUDES.matches("superbiz.business"));
    }

    @Test
    public void jvm() {
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("java.foo"));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("javax.bar"));
    }

    @Test
    public void tomcat() {
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.tomcat."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.el."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.jasper."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.coyote."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.catalina."));
    }

    @Test
    public void tomee() {
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.tomee."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.openejb."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.bval."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.openjpa."));
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.xbean."));
    }

    @Test
    public void commons() {
        assertTrue(ConfigurableListener.DEFAULT_EXCLUDES.matches("org.apache.commons."));
    }
}
