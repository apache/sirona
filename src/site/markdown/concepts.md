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

# Counter

A counter is statistic and concurrency holder. It aggregates the information provided computing
the average, min, max, sum of logs, ....

# Gauge

A gauge is a way to get a measure. It is intended to get a history of a metric.

# DataStore

Counters and Gauges are saved and queried (in memory by default) through a DataStore. it allows you to plug
behind it any kind of persistence you would like.

# StopWatch

A StopWatch is just a handler for a measure with a counter.
