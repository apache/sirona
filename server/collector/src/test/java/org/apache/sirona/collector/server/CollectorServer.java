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
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class CollectorServer {
    private static final Logger LOGGER = Logger.getLogger(CollectorServer.class.getName());

    private final String host;
    private final int port;

    private NioEventLoopGroup workerGroup;

    public CollectorServer(final String host, final int port) {
        this.host = host;
        if (port <= 0) { // generate a port
            this.port = findNextAvailablePort();
        } else {
            this.port = port;
        }
    }
    public int getPort() {
        return port;
    }

    private static int findNextAvailablePort() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (final IOException e) {
                    // no-op
                }
            }
        }
        return 0;
    }

    public CollectorServer start() {
        workerGroup = new NioEventLoopGroup(8);

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_SNDBUF, 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new Initializer())
                .bind(host, port).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        LOGGER.severe("Can't start HTTP server");
                    } else {
                        LOGGER.info(String.format("Server started on http://%s:%s", host, port));
                    }
                }
            }).sync();
        } catch (final InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return this;
    }

    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            LOGGER.info(String.format("Server http://%s:%s stopped", host, port));
        }
    }

    private static class Initializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();

            pipeline
                .addLast("decoder", new HttpRequestDecoder())
                .addLast("inflater", new HttpContentDecompressor())
                .addLast("aggregator", new HttpObjectAggregator( Integer.MAX_VALUE ) )
                .addLast("encoder", new HttpResponseEncoder())
                .addLast("chunked-writer", new ChunkedWriteHandler())
                .addLast("featured-mock-server", new RequestHandler());
        }
    }

    private static class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final Collector collector;

        private RequestHandler() {
            collector = new Collector();
            try { // no need to call destroy since we don't start the timer
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
                    public String getInitParameter(String name) {
                        return null;
                    }

                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        return null;
                    }
                });
            } catch (final ServletException e) {
                // no-op
            }
        }


        protected static byte[] gzipCompression( byte[] unCompress )
            throws IOException
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GZIPOutputStream out = new GZIPOutputStream( buffer );
            out.write( unCompress );
            out.finish();
            ByteArrayInputStream bais = new ByteArrayInputStream( buffer.toByteArray() );
            byte[] res = toByteArray( bais );
            return res;
        }

        public static byte[] toByteArray( InputStream input )
            throws IOException
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer =  new byte[4096];

            int n = 0;
            while ( -1 != ( n = input.read( buffer ) ) )
            {
                output.write( buffer, 0, n );
            }

            return output.toByteArray();
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {
            final ChannelFuture future;
            if (HttpMethod.POST.equals(fullHttpRequest.getMethod())) {
                final InputStream is =
                    new ByteArrayInputStream(gzipCompression(fullHttpRequest.content().toString(Charset.defaultCharset()).getBytes()));

                final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final PrintWriter writer = new PrintWriter(baos);

                collector.doPost(HttpServletRequest.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{HttpServletRequest.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Object.class.equals(method.getDeclaringClass())) {
                            return method.invoke(this, args);
                        }

                        if ("getInputStream".equals(method.getName())) {
                            return new ServletInputStream() {
                                @Override
                                public int read() throws IOException {
                                    return is.read();
                                }
                            };
                        }

                        if ("getHeader".equals( method.getName()) && args[0].equals( "Content-Encoding" )) {
                            return "gzip";
                        }

                        if ("getHeader".equals( method.getName()) && args[0].equals( "Content-Type" )) {
                            return "foo";
                        }

                        throw new UnsupportedOperationException("not implemented: " + method.getName() + " for args: " + Arrays.asList(args));
                    }
                })),
                HttpServletResponse.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { HttpServletResponse.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Object.class.equals(method.getDeclaringClass())) {
                            return method.invoke(this, args);
                        }

                        final String name = method.getName();
                        if ("setStatus".equals(name)) {
                            response.setStatus(HttpResponseStatus.valueOf(Integer.class.cast(args[0])));
                            return null;
                        } else if ("getWriter".equals(name)) {
                            return writer;
                        }

                        throw new UnsupportedOperationException("not implemented");
                    }
                })));

                response.content().writeBytes(baos.toByteArray());
                future = ctx.writeAndFlush(response);
            } else {
                LOGGER.warning("Received " + fullHttpRequest.getMethod());
                future = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            }
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}