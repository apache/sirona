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
package org.apache.sirona.configuration;

import org.apache.sirona.configuration.ioc.IoCs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IoCsTest {
    @Test
    public void field() throws Exception {
        assertEquals("field-value", IoCs.autoSet(new Field()).field);
    }

    @Test
    public void method() throws Exception {
        assertEquals("method-value", IoCs.autoSet(new Method()).value);
    }

    @Test
    public void methodPreventField() throws Exception {
        assertEquals("method-value-again", IoCs.autoSet(new MethodNotField()).methodNotField.value);
    }

    public static class Field {
        private String field;
    }

    public static class Method {
        private String value;

        public void setMethod(final String v) {
            value = v;
        }
    }

    public static class MethodNotField {
        private Method methodNotField = null;

        public void setMethodNotField(final String v) {
            methodNotField = new Method();
            methodNotField.setMethod(v);
        }
    }
}
