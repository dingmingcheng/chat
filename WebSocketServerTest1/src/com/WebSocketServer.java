package com;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServer {
    public void run(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("http-codec", new HttpServerCodec()); // Http消息编码解码
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装
                    pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持
                    pipeline.addLast("handler", new WebSocketServerHandler());
                }                                
            });
            
            Channel channel = b.bind(port).sync().channel();
            
            System.out.println("Web Socket server staarted at port" + port);
            System.out.println("open your browser and navigate to http://localhost:" + port);

            channel.closeFuture().sync();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }        
    }
    
    public static void main(String[] args) {
        int port = 9090;
        new WebSocketServer().run(port);
    }
}
