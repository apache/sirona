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

package org.apache.commons.monitoring;

import java.util.Collection;

/**
 * A composite component that delegates to a primary implementation and
 * maintains a set of secondary instances.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Composite<T>
{
    /**
     * @return the primary instance
     */
    T getPrimary();

    /**
     * @return an (unmodifiable) collection of secondary instances
     */
    Collection<T> getSecondary();

    /**
     * Register a secondary instance
     * @param secondary
     */
    public void addSecondary( T secondary );

    /**
     * Deregister a secondary instance
     * @param secondary
     */
    public void removeSecondary( T secondary );
}
