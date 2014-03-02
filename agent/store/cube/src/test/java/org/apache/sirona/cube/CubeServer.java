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
package org.apache.sirona.cube;

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
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeServer {
    private static final Logger LOGGER = Logger.getLogger(CubeServer.class.getName());

    private final String host;
    private final int port;

    private NioEventLoopGroup workerGroup;
    private final Collection<String> messages = new LinkedList<String>();

    public CubeServer(final String host, final int port) {
        this.host = host;
        if (port <= 0) { // generate a port
            this.port = findNextAvailablePort();
        } else {
            this.port = port;
        }
    }

    public Collection<String> getMessages() {
        synchronized (messages) {
            return new ArrayList<String>(messages);
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

    public CubeServer start() {
        workerGroup = new NioEventLoopGroup(8);

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_SNDBUF, 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new Initializer(messages))
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
        private final Collection<String> messages;

        private Initializer(final Collection<String> messages) {
            this.messages = messages;
        }

        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();

            pipeline
                .addLast("decoder", new HttpRequestDecoder())
                .addLast("inflater", new HttpContentDecompressor())
                .addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast("encoder", new HttpResponseEncoder())
                .addLast("chunked-writer", new ChunkedWriteHandler())
                .addLast( "featured-mock-server", new RequestHandler( messages ) );
        }
    }

    private static class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final Collection<String> messages;

        private RequestHandler(final Collection<String> messages) {
            this.messages = messages;
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {
            final ChannelFuture future;
            if (HttpMethod.POST.equals(fullHttpRequest.getMethod())) {
                String message = fullHttpRequest.content().toString(Charset.defaultCharset());
                synchronized (messages) {
                    messages.add(message);
                }
                final HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                future = ctx.writeAndFlush(response);
            } else {
                LOGGER.warning("Received " + fullHttpRequest.getMethod());
                future = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            }

            future.addListener(ChannelFutureListener.CLOSE);
        }
    }



}