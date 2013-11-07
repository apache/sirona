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
## Do a measure manually using a StopWatch

<pre class="prettyprint linenums"><![CDATA[
final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, "my counter"));
final StopWatch stopWatch = Repository.INSTANCE.start(counter);
// do something
stopwatch.stop();
]]></pre>

## Create a custom Gauge

<pre class="prettyprint linenums"><![CDATA[
public class MyRandomGauge implements Gauge {
    public static final Role MY_RANDOM_ROLE = new Role("Random", Unit.UNARY);

    @Override
    public Role role() {
        return MY_RANDOM_ROLE;
    }

    @Override
    public double value() {
        return new Random(System.currentTimeMillis()).nextDouble();
    }

    @Override
    public long period() { // millisecond
        return 4000;
    }
}
]]></pre>

The role identifies the gauge specifying its unit (Unit.UNARY means "value" - absolute, percent...).

The value method is the one called to get the measure.

Period method defines when to do a measure using the gauge (it implicitly defines a a timer).

## Get a gauge values

Gauges values are retrieved by interval:

<pre class="prettyprint linenums"><![CDATA[
Map<Long, Double> sortedValueByIncreasingDate = Repository.INSTANCE.getGaugeValues(start, end, gaugeRole);
]]></pre>

## Monitor JDBC

To monitor JDBC just configure your DataSource replacing its `java.sql.Driver` by `org.apache.sirona.jdbc.MonitoringDriver`
and updating its jdbc url from `jdbc:foo:bar` to `jdbc:monitoring:foo:bar?delegateDriver=xxxxx`.

Note: delegateDriver needs to be the last parameter (if there are several).
