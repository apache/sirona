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
package org.apache.sirona.boomerang;

import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BoomerangServletTest {
    @Before
    @After
    public void init() {
        Repository.INSTANCE.reset();
    }

    @Test
    public void collect() throws ServletException, IOException {
        final BoomerangServlet servlet = new BoomerangServlet();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        servlet.init(ServletConfig.class.cast(
                Proxy.newProxyInstance(loader, new Class<?>[]{ServletConfig.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        return null;
                    }
                })
        ));
        try {
            servlet.service(
                    HttpServletRequest.class.cast(
                        Proxy.newProxyInstance(loader, new Class<?>[]{HttpServletRequest.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                if ("getQueryString".equals(method.getName())) {
                                    return "v=0.9&u=http%3A%2F%2Flocalhost%2Fboomerang%2Ftest.html&rt.start=navigation" +
                                            "&rt.bstart=1391971905166&rt.end=1391971905170&t_done=146&t_resp=6" +
                                            "&t_page=140&r=&t_other=boomerang%7C2%2Cboomr_fb%7C142&bw=NaN&bw_err=NaN" +
                                            "&lat=19&lat_err=2.85&bw_time=1391971906";
                                }
                                return null;
                            }
                        })),
                    HttpServletResponse.class.cast(
                        Proxy.newProxyInstance(loader, new Class<?>[]{HttpServletResponse.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                if ("getWriter".equals(method.getName())) {
                                    return new PrintWriter(new ByteArrayOutputStream()); // don't care
                                }
                                return null;
                            }
                        })));
        } finally {
            servlet.destroy();
        }

        final Counter perceived = Repository.INSTANCE.getCounter(new Counter.Key(BoomerangServlet.BOOMERANG_PERCEIVED, "/boomerang/test.html"));
        assertNotNull(perceived);
        assertEquals(1, perceived.getHits());
        assertEquals(146., perceived.getMax(), 0.);

        final Counter latency = Repository.INSTANCE.getCounter(new Counter.Key(BoomerangServlet.BOOMERANG_LATENCY, "/boomerang/test.html"));
        assertNotNull(latency);
        assertEquals(1, latency.getHits());
        assertEquals(19., latency.getMax(), 0.);
    }
}
