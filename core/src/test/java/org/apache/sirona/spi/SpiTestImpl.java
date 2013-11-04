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
package org.apache.sirona.spi;

import org.apache.sirona.status.Status;
import org.apache.sirona.status.Validation;
import org.apache.sirona.status.ValidationResult;

import java.util.Arrays;

public class SpiTestImpl extends DefaultSPI {
    public static ValidationResult status = new ValidationResult("n", Status.OK, "m");

    @Override
    public <T> Iterable<T> find(final Class<T> api, final ClassLoader loader) {
        if (Validation.class.equals(api)) {
            return (Iterable<T>) Arrays.asList(new Validation() {
                @Override
                public ValidationResult validate() {
                    return status;
                }
            });
        }
        return super.find(api, loader);
    }
}
