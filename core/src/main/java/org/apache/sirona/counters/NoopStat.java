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
package org.apache.sirona.counters;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public class NoopStat implements StorelessUnivariateStatistic {
    public static final NoopStat INSTANCE = new NoopStat();

    public NoopStat() {
        // no-op
    }

    @Override
    public void increment(final double d) {
        // no-op
    }

    @Override
    public void incrementAll(final double[] values) throws MathIllegalArgumentException {
        // no-op
    }

    @Override
    public void incrementAll(final double[] values, final int start, final int length) throws MathIllegalArgumentException {
        // no-op
    }

    @Override
    public double getResult() {
        return 0;
    }

    @Override
    public long getN() {
        return 0;
    }

    @Override
    public void clear() {
        // no-op
    }

    @Override
    public double evaluate(final double[] values) throws MathIllegalArgumentException {
        return 0;
    }

    @Override
    public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
        return 0;
    }

    @Override
    public StorelessUnivariateStatistic copy() {
        return null;
    }
}
