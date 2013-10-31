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
# Manually (clients)

To handle manually the interception you need to import commons-monitoring-aop.
Then you can rely on `org.apache.sirona.aop.MonitoringProxyFactory`.

`org.apache.commons.proxy.ProxyFactory` key defines the proxy factory to use to create proxies For instance
to use javassist you set it to `org.apache.commons.proxy.factory.javassist.JavassistProxyFactory`
(and you'll include javassist in your application).

Then the API is quite simple:

```java
final MyClient client = MonitoringProxyFactory.monitor(MyClient.class, getMyClientInstance());
```

# CDI

You just need to decorate your CDI bean/method with the interceptor binding `org.apache.sirona.cdi.Monitored`.

For instance:


    @Monitored
    @ApplicationScoped
    public class MyMonitoredBean {
        public void myMethod() {
            // ...
        }
    }

Note: in some (old) CDI implementation you'll need to activate the monitoring interceptor: `org.apache.sirona.cdi.SironaInterceptor`.

You can configure it (without adding the `@Monitored` annotation) using `org.apache.sirona.cdi.performance` key. The
value is a list of predicate (`regex:<regex>`, `prefix:<prefix>`, `suffix:<suffix>`).

For instance:

```
org.apache.sirona.cdi.performance = prefix:org.superbiz.MyService,regex:.*Bean
```

# Spring

Using `org.apache.sirona.spring.BeanNameMonitoringAutoProxyCreator` you can automatically
add monitoring to selected beans.

    <bean class="org.apache.sirona.spring.BeanNameMonitoringAutoProxyCreator">
      <property name="beanNames">
        <list>
          <value>*Service</value>
        </list>
      </property>
    </bean>

An alternative is to use `org.apache.sirona.spring.PointcutMonitoringAutoProxyCreator` which uses
a `org.springframework.aop.Pointcut` to select beans to monitor.

# AspectJ

To use AspectJ weaver (it works with build time enhancement too but it is often less relevant) just configure a custom
concrete aspect defining the pointcut to monitor:

    <aspectj>
      <aspects>
        <concrete-aspect name="org.apache.commons.aspectj.MyMonitoringAspectJ"
                         extends="org.apache.sirona.aspectj.SironaAspect">
          <pointcut name="pointcut" expression="execution(* org.apache.sirona.aspectj.AspectJMonitoringTest$MonitorMe.*(..))"/>
        </concrete-aspect>
      </aspects>

      <weaver>
        <include within="org.apache.sirona.aspectj.AspectJMonitoringTest$MonitorMe"/>
      </weaver>
    </aspectj>

See [AspectJ documentation](http://eclipse.org/aspectj/doc/next/progguide/language-joinPoints.html) for more information.

# Note on interceptor configuration (experimental)

Few global configuration (`sirona.properties`) is available for all interceptors:

* `org.apache.sirona.performance.adaptive`: if this boolean is set to true the following parameters are taken into account
* `org.apache.sirona.performance.threshold`: if > 0 it is the duration under which calls are skipped (no more monitored). Note: the format supports [duration] [TimeUnit name] too. For instance `100 MILLISECONDS` is valid.
* `org.apache.sirona.performance.forced-iteration`: the number of iterations a deactivated interceptor (because of threshold rule) will wait before forcing a measure to see if the monitoring should be activated back.

Note: `threshold` and `forced-iteration` parameters can be specialized appending to `org.apache.sirona.` the method qualified name.

Here a sample of the behavior associated with these properties. Let say you configured `forced-iteration` to 5 and
 `threshold` to 100 milliseconds. If `xxx ms` represent an invocation of xxx milliseconds and `*` represent a call
 which was measured, here is an invocation sequence:

 ```
 500 ms*
 5 ms*
 500 ms
 500 ms
 500 ms
 500 ms
 500 ms
 20 ms*
 200 ms
 200 ms
 200 ms
 200 ms
 200 ms
 500 ms*
 500 ms*
 ```

Note: the idea is to reduce the overhead of the interception. This is pretty efficient in general but particularly with AspectJ.
Note 2: if your invocations are pretty unstable this is not really usable since since you'll not get a good threshold value.
