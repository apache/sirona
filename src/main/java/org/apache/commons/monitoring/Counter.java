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

/**
 * A counter to collect application processed items (bytes received, lines processed by a batch,
 * time elapsed by some processing ...).
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface Counter
    extends StatValue
{

    /**
     * Add value to the counter. Delta should not be negative (in such case a Gauge should be used).
     * @param delta
     */
    void add( long delta, Unit unit );

    /**
     * @return the sum of all set operations
     */
    long getSum();

    /**
     * @return how many time the value has been set
     */
    int getHits();

}

