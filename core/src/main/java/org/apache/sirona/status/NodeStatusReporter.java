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
package org.apache.sirona.status;

import org.apache.sirona.spi.SPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class NodeStatusReporter {
    public synchronized NodeStatus computeStatus() {
        final Validation[] validations = reload();

        final Collection<ValidationResult> results = new ArrayList<ValidationResult>(validations.length);
        for (final Validation v : validations) {
            results.add(v.validate());
        }
        return new NodeStatus(results.toArray(new ValidationResult[results.size()]), new Date());
    }

    public synchronized Validation[] reload() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final Collection<Validation> val = new LinkedList<Validation>();
        for (final Validation v : SPI.INSTANCE.find(Validation.class, classLoader)) {
            val.add(v);
        }
        for (final ValidationFactory f : SPI.INSTANCE.find(ValidationFactory.class, classLoader)) {
            val.addAll(Arrays.asList(f.validations()));
        }

        return val.toArray(new Validation[val.size()]);
    }
}
