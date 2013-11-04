<!---
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# Repository

The repository is a singleton for the JVM. It is the entry point to get access to counters and gauges.

    public interface Repository extends Iterable<Counter> {
        Counter getCounter(Counter.Key key);
        void clear();
        StopWatch start(Counter counter);

        Map<Long, Double> getGaugeValues(long start, long end, Role role);
        void stopGauge(Role role);
    }

# Counter

A counter is a statistic and concurrency holder. It aggregates the information provided computing
the average, min, max, sum of logs, ....


    public interface Counter {
        Key getKey();
        void reset();

        void add(double delta);

        AtomicInteger currentConcurrency();
        int getMaxConcurrency();

        double getMax();
        double getMin();
        long getHits();
        double getSum();
        double getStandardDeviation();
        double getVariance();
        double getMean();
        double getSecondMoment();
    }

# Gauge

A gauge is a way to get a measure. It is intended to get a history of a metric.

    public interface Gauge {
        Role role();
        double value();
        long period();
    }

# StopWatch

A StopWatch is just a handler for a measure with a counter.

    public interface StopWatch {
        long getElapsedTime();

        StopWatch stop();
    }

# Node status

Node statuses can be reported using `org.apache.sirona.status.Validation`. `Validation` and `ValidationFactory`
(just a list of validation) can be registered using `SPI` mecanism (`META-INF/services/org.apache.sirona.status.Validation`
and `META-INF/services/org.apache.sirona.status.ValidationFactory` by default).

`Validation` API is the following one:

```java
public interface Validation {
    ValidationResult validate();
}
```

A `ValidationResult` is just a message, a validation name and a status. It is aggregated by node to compute
the node status keeping the lowest of all statuses of validation results.

# DataStore

Counters, Gauges and status are saved and queried (in memory by default) through a DataStore. it allows you to plug
behind it any kind of persistence you would like. There are generally two kind of stores: local or remote.

Here are the entry points if you want more details:

* `org.apache.sirona.store.counter.CounterDataStore`
* `org.apache.sirona.store.gauge.GaugeDataStore`
* `org.apache.sirona.store.status.NodeStatusDataStore`
* `org.apache.sirona.store.counter.CollectorCounterStore`
* `org.apache.sirona.store.gauge.CollectorGaugeDataStore`
