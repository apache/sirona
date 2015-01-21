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
package org.apache.sirona.collector.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.DelegatedCollectorGaugeDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegistrationTest {
    private HttpServer agent;

    @Before
    public void start() {
        agent = new HttpServer("localhost", Integer.getInteger("collector.server.port", 1234)).start();
        Repository.INSTANCE.reset();
        DelegatedCollectorGaugeDataStore.class.cast(IoCs.getInstance(CollectorGaugeDataStore.class)).reset();
    }

    @After
    public void shutdown() {
        agent.stop();
        Repository.INSTANCE.reset();
    }

    @Test
    public void pull() throws ServletException, InterruptedException {
        final Collector collector = new Collector();
        try {
            collector.init(new ServletConfig() {
                @Override
                public String getServletName() {
                    return null;
                }

                @Override
                public ServletContext getServletContext() {
                    return null;
                }

                @Override
                public String getInitParameter(final String name) {
                    if (name.endsWith("period")) {
                        return "100";
                    }
                    return "http://localhost:"+Integer.getInteger("collector.server.port", 1234); // agent-urls
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }
            });

            Thread.sleep( 600 );

            final Collection<Counter> counters = Repository.INSTANCE.counters();
            assertEquals(counters.toString(), 3, counters.size());

            final Collection<Role> gauges = Repository.INSTANCE.gauges();
            assertEquals(gauges.toString(), 3, gauges.size());

            assertTrue(Repository.INSTANCE.statuses().containsKey("ubuntu"));
        } finally {
            collector.destroy();
        }
    }

    public static class HttpServer {
        private final String host;
        private final int port;

        private NioEventLoopGroup workerGroup;

        public HttpServer(final String host, final int port) {
            this.host = host;
            this.port = port;
        }

        public HttpServer start() {
            workerGroup = new NioEventLoopGroup(8);

            try {
                final ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap
                    .option(ChannelOption.SO_REUSEADDR, true) //
                    .option(ChannelOption.SO_SNDBUF, 1024) //
                    .option(ChannelOption.TCP_NODELAY, true) //
                    .group(workerGroup) //
                    .channel(NioServerSocketChannel.class) //
                    .childHandler(new Initializer()) //
                    .bind( host, port ).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(final ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                throw new SironaException("bind failed");
                            }
                        }
                    }).sync();
            } catch (final InterruptedException e) {
                throw new SironaException(e);
            }

            return this;
        }

        public void stop() {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }

        private static class Initializer extends ChannelInitializer<SocketChannel> {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();

                pipeline
                    .addLast("decoder", new HttpRequestDecoder()) //
                    .addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE)) //
                    .addLast("encoder", new HttpResponseEncoder()) //
                    .addLast("chunked-writer", new ChunkedWriteHandler()) //
                    .addLast( "server", new RequestHandler() );
            }
        }

        private static class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
            private AtomicBoolean done = new AtomicBoolean(false);

            @Override
            protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {
                if (done.get()) {
                    ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("[]".getBytes())))
                        .addListener(ChannelFutureListener.CLOSE);
                    return;
                }

                done.set(true);
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, //
                                                              HttpResponseStatus.OK, //
                                                              Unpooled.copiedBuffer(("" +
                    "[{\"type\": \"counter\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"min\":2.0,\"unit\":\"ns\",\"hits\":1,\"max\":2.0,\"marker\":\"ubuntu\",\"name\":\"counter#2\",\"concurrency\":0,\"m2\":0.0,\"sum\":2.0,\"mean\":2.0,\"role\":\"performances\",\"variance\":0.0}},{\"type\": \"counter\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"min\":0.0,\"unit\":\"ns\",\"hits\":1,\"max\":0.0,\"marker\":\"ubuntu\",\"name\":\"counter#0\",\"concurrency\":0,\"m2\":0.0,\"sum\":0.0,\"mean\":0.0,\"role\":\"performances\",\"variance\":0.0}},{\"type\": \"counter\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"min\":1.0,\"unit\":\"ns\",\"hits\":1,\"max\":1.0,\"marker\":\"ubuntu\",\"name\":\"counter#1\",\"concurrency\":0,\"m2\":0.0,\"sum\":1.0,\"mean\":1.0,\"role\":\"performances\",\"variance\":0.0}},{\"type\": \"gauge\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"unit\":\"u\",\"marker\":\"ubuntu\",\"value\":0.87,\"role\":\"CPU\"}},{\"type\": \"gauge\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"unit\":\"u\",\"marker\":\"ubuntu\",\"value\":1.0245232E7,\"role\":\"Used Memory\"}},{\"type\": \"gauge\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"unit\":\"u\",\"marker\":\"ubuntu\",\"value\":0.0,\"role\":\"gaugerole\"}},{\"type\": \"validation\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"message\":\"descr\",\"marker\":\"ubuntu\",\"status\":\"OK\",\"name\":\"fake\"}},{\"type\": \"validation\",\"time\": \"2013-11-07T10:31:13Z\",\"data\": {\"message\":\"descr\",\"marker\":\"ubuntu\",\"status\":\"OK\",\"name\":\"refake\"}}]" +
                    "").getBytes())))
                    .addListener( ChannelFutureListener.CLOSE );
            }
        }
    }
}
