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
package org.apache.sirona.predicate;

import org.apache.sirona.configuration.predicate.PredicateEvaluator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PredicateTest
{
    @Test
    public void evaluate() {
        final PredicateEvaluator evaluator = new PredicateEvaluator(
            "regex:.+Foo," +
            "regex:Bar.*," +
            "regex:[0-9]+," +
            "prefix:asf," +
            "prefix:sirona," +
            "prefix:!excluded," +
            "suffix:tiger," +
            "suffix:Cat", ",");

        assertTrue(evaluator.matches("AnythingFoo"));
        assertFalse(evaluator.matches("Foo"));
        assertTrue(evaluator.matches("Bar"));
        assertTrue(evaluator.matches("Barcedc"));
        assertFalse(evaluator.matches("azdphdBar"));
        assertFalse(evaluator.matches("fooasf"));
        assertTrue(evaluator.matches("asf"));
        assertFalse(evaluator.matches("excluded"));
        assertTrue(evaluator.matches("asfcdc"));
        assertTrue(evaluator.matches("sironaaaaaaa"));
        assertTrue(evaluator.matches("TomEE the tiger"));
        assertFalse(evaluator.matches("cat"));
        assertTrue(evaluator.matches("Cat"));
        assertTrue(evaluator.matches("1283"));
        assertTrue(evaluator.matches("Ends with Cat"));
    }

    @Test
    public void allFalse() {
        assertFalse(new PredicateEvaluator("true:!true",",").matches("or"));
    }
}
